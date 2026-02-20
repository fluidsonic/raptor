package io.fluidsonic.raptor.service2

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.event.*
import kotlin.reflect.*
import kotlinx.datetime.*


@Deprecated("Not yet implemented.", level = DeprecationLevel.WARNING)
@RaptorDsl
context(_: RaptorServiceComponent2<Service>)
public fun <Service : RaptorService2> onEveryDay(time: LocalTime, timeZone: TimeZone): RaptorServiceInput2<Service, LocalDate> =
	DailyScheduleInputSource(time = time, timeZone = timeZone)


@RaptorDsl
context(_: RaptorServiceComponent2<*>)
public fun <Service : RaptorService2, Event : RaptorEvent> onEvent(
	event: KClass<Event>,
): RaptorServiceInput2<Service, Event> =
	DefaultEventInputSource(event)


@Deprecated("Not yet implemented.", level = DeprecationLevel.WARNING)
@JvmName("onScheduledTaskNoData")
@RaptorDsl
context(_: RaptorServiceComponent2<Service>)
public inline fun <Service : RaptorService2, reified Key : Any> onScheduledTask(): RaptorServiceInput2<Service, Key> =
	@Suppress("DEPRECATION") onScheduledTask(dataType = typeOf<Key>())


@Deprecated("Not yet implemented.", level = DeprecationLevel.WARNING)
@JvmName("onScheduledTaskNoData")
@RaptorDsl
context(_: RaptorServiceComponent2<Service>)
public fun <Service : RaptorService2, Key : Any> onScheduledTask(dataType: KType): RaptorServiceInput2<Service, Key> =
	ScheduledTaskInputSource<Service, Key, Key>(dataType = dataType, keyType = dataType).map { it.first }


@Deprecated("Not yet implemented.", level = DeprecationLevel.WARNING)
@RaptorDsl
context(_: RaptorServiceComponent2<Service>)
public inline fun <Service : RaptorService2, reified Key : Any, reified Data : Any> onScheduledTask(): RaptorServiceInput2<Service, Pair<Key, Data>> =
	@Suppress("DEPRECATION") onScheduledTask(keyType = typeOf<Key>(), dataType = typeOf<Data>())


@Deprecated("Not yet implemented.", level = DeprecationLevel.WARNING)
@RaptorDsl
context(_: RaptorServiceComponent2<Service>)
public fun <Service : RaptorService2, Key : Any, Data : Any> onScheduledTask(keyType: KType, dataType: KType): RaptorServiceInput2<Service, Pair<Key, Data>> =
	ScheduledTaskInputSource(dataType = dataType, keyType = keyType)


@Deprecated("Not yet implemented.", level = DeprecationLevel.WARNING)
@RaptorDsl
context(_: RaptorServiceComponent2<Service>)
public fun <Service : RaptorService2, Key : Any, Value : Any, Queue> onQueue(
	queueProperty: KProperty1<Service, Queue>,
): RaptorServiceInput2<Service, Pair<Key, Value>> =
	QueueInputSource(queueProperty)


@RaptorDsl
context(_: RaptorServiceComponent2<Service>)
public fun <Service : RaptorService2> onStart(): RaptorServiceInput2<Service, Unit> =
	StartInputSource
