package io.fluidsonic.raptor


@RaptorDsl
public interface RaptorComponentSet2<out Component : RaptorComponent2> {

	@RaptorDsl
	public fun all(configure: Component.() -> Unit)
}
