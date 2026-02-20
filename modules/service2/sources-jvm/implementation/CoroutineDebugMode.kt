package io.fluidsonic.raptor.service2

import kotlinx.coroutines.*


internal object CoroutineDebugMode {

	val isEnabled: Boolean = when (System.getProperty(DEBUG_PROPERTY_NAME)) {
		DEBUG_PROPERTY_VALUE_OFF -> false
		DEBUG_PROPERTY_VALUE_ON, "" -> true
		else -> CoroutineDebugMode::class.java.desiredAssertionStatus()
	}
}
