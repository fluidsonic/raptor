package io.fluidsonic.raptor

import kotlin.time.*
import kotlinx.datetime.*


public sealed class RaptorJobTiming {

	public data class AtDateTime(val dateTime: LocalDateTime, val timeZone: TimeZone) : RaptorJobTiming()
	public data class AtInterval(val interval: Duration) : RaptorJobTiming()
	public data class DailyAtTime(val time: LocalTime, val timeZone: TimeZone) : RaptorJobTiming()
}
