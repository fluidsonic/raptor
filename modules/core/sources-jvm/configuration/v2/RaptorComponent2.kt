package io.fluidsonic.raptor


@RaptorDsl
public interface RaptorComponent2 {

	@RaptorDsl
	public val extensions: RaptorComponentExtensionSet

	public fun RaptorComponentConfigurationEndScope2.onConfigurationEnded(): Unit = Unit
	public fun RaptorComponentConfigurationStartScope2.onConfigurationStarted(): Unit = Unit


	public companion object


	@RaptorDsl
	public abstract class Base<Component : RaptorComponent2> : RaptorComponent2, RaptorAssemblyQuery2<Component> {

		@RaptorDsl
		final override val extensions: RaptorComponentExtensionSet = DefaultRaptorComponentExtensionSet()


		@RaptorDsl
		@Suppress("UNCHECKED_CAST")
		override fun each(configure: Component.() -> Unit) {
			(this as Component).configure()
		}
	}
}
