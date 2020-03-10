package io.fluidsonic.raptor


class RaptorSetupRegistration<out Element : Any> internal constructor(
	val element: Element,
	val tags: Set<Any> = emptySet()
)
