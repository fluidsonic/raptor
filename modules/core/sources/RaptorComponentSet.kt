package io.fluidsonic.raptor


@RaptorDsl
interface RaptorComponentSet<out Component : RaptorComponent> {

	@RaptorDsl
	fun configure(action: Component.() -> Unit)
}


@RaptorDsl
operator fun <Component : RaptorComponent> RaptorComponentSet<Component>.invoke(action: Component.() -> Unit) =
	configure(action)
