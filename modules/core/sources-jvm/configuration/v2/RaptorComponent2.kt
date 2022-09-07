package io.fluidsonic.raptor


@RaptorDsl
public interface RaptorComponent2 {

	@RaptorDsl
	public val extensions: RaptorComponentExtensionSet

	public fun RaptorComponentConfigurationEndScope2.onConfigurationEnded(): Unit = Unit
	public fun RaptorComponentConfigurationStartScope2.onConfigurationStarted(): Unit = Unit


	public companion object


	@RaptorDsl
	public abstract class Base : RaptorComponent2 {

		@RaptorDsl
		final override val extensions: RaptorComponentExtensionSet = DefaultRaptorComponentExtensionSet()
	}
}


@RaptorDsl
public operator fun <Component : RaptorComponent2> Component.invoke(block: Component.() -> Unit) {
	apply(block)
}
