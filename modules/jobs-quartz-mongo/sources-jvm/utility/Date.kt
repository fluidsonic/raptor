package io.fluidsonic.raptor

import io.fluidsonic.time.*
import java.util.*
import kotlinx.datetime.*


// TODO move to fluid-time

public fun Date.toKotlinInstant(): Timestamp =
	Instant.fromEpochMilliseconds(time)
