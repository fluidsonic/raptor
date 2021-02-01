package io.fluidsonic.raptor

import com.mongodb.client.*
import com.novemberain.quartz.mongodb.*
import io.fluidsonic.stdlib.*
import java.util.*
import kotlin.collections.set
import kotlin.time.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.json.*
import org.quartz.*
import org.quartz.impl.*
import org.quartz.simpl.*
import org.slf4j.*


internal class QuartzJobScheduler(
	private val context: RaptorContext,
	private val database: MongoDatabase,
	private val dispatcher: CoroutineDispatcher,
	private val registry: RaptorJobRegistry,
) : RaptorJobScheduler {

	private val json: Json = Json
	private var quartzScheduler: Scheduler? = null


	private fun createQuartzScheduler() =
		DirectSchedulerFactory.getInstance()
			.run {
				val name = "JobScheduler ${schedulerIndex.incrementAndGet()}"

				createScheduler(
					name,
					SimpleInstanceIdGenerator().generateInstanceId(),
					SimpleThreadPool(100, Thread.NORM_PRIORITY), // FIXME ok?
					MongoDBJobStore(database),
				)

				getScheduler(name)
			}
			.also { it.jobScheduler = this }


	@Suppress("UNCHECKED_CAST")
	private suspend fun executeJob(executionContext: JobExecutionContext) {
		val key = executionContext.jobDetail.key
		val executor = registry[key.group]
			.ifNull { error("Cannot execute job '${key.name}' in group '${key.group}' as no executor has been registered for that group.") }
			.let { it as RaptorJobExecutor<Any?> }
		// FIXME logging
		if (executor.group.serializer === Unit.serializer())
			executor.execute(context, Unit)
		else {
			val data = executionContext.mergedJobDataMap["data"]
				?.let { it as? String }
				.ifNull { error("Data missing for job '${key.name}' in group '${key.group}'.") }
				.let { string ->
					try {
						json.decodeFromString(deserializer = executor.group.serializer, string = string)
					}
					catch (e: Throwable) {
						throw JobExecutionException("Cannot decode data for job '${key.name}' in group '${key.group}': $string", e)
					}
				}

			executor.execute(context, data)
		}
	}


	override suspend fun queryStatus(id: String, group: RaptorJobGroup<*>): RaptorJobStatus? =
		withContext(dispatcher) {
			val quartzScheduler = checkNotNull(quartzScheduler) { "JobScheduler isn't started." }

			quartzScheduler.getTrigger(TriggerKey(id, group.id))?.let { trigger ->
				RaptorJobStatus(
					lastExecutionTimestamp = trigger.previousFireTime?.toKotlinInstant()
				)
			}
		}


	override suspend fun remove(id: String, group: RaptorJobGroup<*>) {
		logger.debug("Removing job '$id' in group '${group.id}'.")

		withContext(dispatcher) {
			val quartzScheduler = checkNotNull(quartzScheduler) { "JobScheduler isn't started." }

			quartzScheduler.deleteJob(JobKey(id, group.id))
		}
	}


	override suspend fun <Data> schedule(id: String, group: RaptorJobGroup<Data>, data: Data, timing: RaptorJobTiming) {
		logger.debug("Scheduling job '$id' in group '${group.id}' with timing '$timing'.")

		withContext(dispatcher) {
			val quartzScheduler = checkNotNull(quartzScheduler) { "JobScheduler isn't started." }
			val encodedData = json.encodeToString(serializer = group.serializer, value = data)

			val key = JobKey(id, group.id)
			val existingDetail: JobDetail? = quartzScheduler.getJobDetail(key)
			if (
				existingDetail == null
				|| existingDetail.jobClass != jobRunnerClass
				|| !existingDetail.isDurable
				|| existingDetail.jobDataMap["data"] != encodedData
			) {
				if (existingDetail == null)
					logger.debug("Adding job '$id' in group '${group.id}' to database…")
				else
					logger.debug("Updating job '$id' in group '${group.id}' in database…")

				quartzScheduler.addJob(
					JobBuilder.newJob()
						.withIdentity(key)
						.ofType(jobRunnerClass)
						.requestRecovery(true)
						.storeDurably(true)
						.usingJobData("data", encodedData)
						.build(),
					true
				)
			}

			val triggerKey = TriggerKey(key.name, key.group)

			val existingTrigger = quartzScheduler.getTrigger(triggerKey)
			if (existingTrigger == null || !timing.isValidTrigger(existingTrigger)) {
				val trigger = TriggerBuilder
					.newTrigger()
					.withIdentity(triggerKey)
					.forJob(key)
					.startNow()
					.withSchedule(timing.createSchedule())
					.build()

				if (existingTrigger == null) {
					logger.debug("Adding trigger for job '$id' in group '${group.id}' to database…")
					quartzScheduler.scheduleJob(trigger)
				}
				else {
					logger.debug("Updating trigger for job '$id' in group '${group.id}' in database…")
					quartzScheduler.rescheduleJob(triggerKey, trigger)
				}
			}
		}
	}


	suspend fun start() {
		withContext(dispatcher) {
			check(quartzScheduler == null)

			createQuartzScheduler()
				.also { quartzScheduler = it }
				.start()
		}
	}


	suspend fun stop() {
		withContext(dispatcher) {
			checkNotNull(quartzScheduler).shutdown(true)

			quartzScheduler = null
		}
	}


	companion object {

		private const val quartzSchedulerContextKey = "JobScheduler"

		private val jobRunnerClass = QuartzJobRunner::class.java
		private val logger = LoggerFactory.getLogger(RaptorJobScheduler::class.java)!!
		private val schedulerIndex = atomic(0)


		suspend fun executeJob(executionContext: JobExecutionContext) {
			executionContext.scheduler.jobScheduler.executeJob(executionContext = executionContext)
		}


		private var Scheduler.jobScheduler
			get() = context[quartzSchedulerContextKey] as QuartzJobScheduler
			set(value) {
				context[quartzSchedulerContextKey] = value
			}
	}
}


@OptIn(ExperimentalTime::class)
private fun RaptorJobTiming.createSchedule() =
	when (this) {
		is RaptorJobTiming.AtDateTime ->
			CronScheduleBuilder.cronSchedule(cronExpression)
				.inTimeZone(TimeZone.getTimeZone(timeZone.id))
				.withMisfireHandlingInstructionFireAndProceed()

		is RaptorJobTiming.AtInterval ->
			SimpleScheduleBuilder.simpleSchedule()
				.withIntervalInMilliseconds(interval.toLongMilliseconds())
				.withMisfireHandlingInstructionNowWithExistingCount()

		is RaptorJobTiming.DailyAtTime ->
			CronScheduleBuilder.cronSchedule(cronExpression)
				.inTimeZone(TimeZone.getTimeZone(timeZone.id))
				.withMisfireHandlingInstructionFireAndProceed()
	}


@OptIn(ExperimentalTime::class)
private fun RaptorJobTiming.isValidTrigger(trigger: Trigger) =
	when (this) {
		is RaptorJobTiming.AtDateTime ->
			trigger is CronTrigger
				&& trigger.calendarName == null
				&& trigger.cronExpression == cronExpression
				&& trigger.endTime == null
				&& trigger.timeZone?.id == timeZone.id

		is RaptorJobTiming.AtInterval ->
			trigger is SimpleTrigger
				&& trigger.calendarName == null
				&& trigger.endTime == null
				&& trigger.repeatCount == SimpleTrigger.REPEAT_INDEFINITELY
				&& trigger.repeatInterval == interval.toLongMilliseconds()

		is RaptorJobTiming.DailyAtTime ->
			trigger is CronTrigger
				&& trigger.calendarName == null
				&& trigger.cronExpression == cronExpression
				&& trigger.endTime == null
				&& trigger.timeZone?.id == timeZone.id
	}


private val RaptorJobTiming.AtDateTime.cronExpression
	get() = "${dateTime.second} ${dateTime.minute} ${dateTime.hour} ${dateTime.dayOfMonth} ${dateTime.monthNumber - 1} ? ${dateTime.year}"


private val RaptorJobTiming.DailyAtTime.cronExpression
	get() = "${time.second} ${time.minute} ${time.hour} * * * *"
