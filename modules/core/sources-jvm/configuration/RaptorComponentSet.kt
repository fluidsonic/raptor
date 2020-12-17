package io.fluidsonic.raptor


@RaptorDsl
public interface RaptorComponentSet<out Component : RaptorComponent> {

	@RaptorDsl
	public fun configure(action: Component.() -> Unit)
}


@RaptorDsl
public operator fun <Component : RaptorComponent> RaptorComponentSet<Component>.invoke(action: Component.() -> Unit) {
	configure(action)
}
