package io.fluidsonic.raptor

import kotlin.reflect.*


internal fun KClass<*>.defaultGraphName() =
	simpleName ?: error("Cannot derive name from Kotlin $this. It must be defined explicitly: name(\"…\")")
