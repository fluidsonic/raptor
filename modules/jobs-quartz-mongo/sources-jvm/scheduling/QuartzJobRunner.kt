package io.fluidsonic.raptor

import kotlinx.coroutines.*
import org.quartz.*
import org.quartz.Job


@DisallowConcurrentExecution
@PersistJobDataAfterExecution
internal class QuartzJobRunner : Job {

	override fun execute(context: JobExecutionContext) {
		runBlocking {
			QuartzJobScheduler.executeJob(context)
		}
	}
}
