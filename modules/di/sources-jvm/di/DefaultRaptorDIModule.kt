package io.fluidsonic.raptor

import kotlin.reflect.*


internal class DefaultRaptorDIModule(
	val name: String,
	val provideByType: Map<KType, RaptorDI.() -> Any?>,
)
