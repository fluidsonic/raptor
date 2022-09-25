package io.fluidsonic.raptor


@RaptorDsl
public interface RaptorComponent<Component : RaptorComponent<Component>> : RaptorAssemblyQuery<Component> {

	@RaptorDsl
	public val extensions: RaptorComponentExtensionSet

	// TODO Can we improve the API to not have this all over the DSL?
	@RaptorDsl
	public val plugin: RaptorPluginWithConfiguration<*>

	public fun RaptorComponentConfigurationEndScope<Component>.onConfigurationEnded(): Unit = Unit
	public fun RaptorComponentConfigurationStartScope.onConfigurationStarted(): Unit = Unit


	@RaptorDsl
	@Suppress("UNCHECKED_CAST")
	override fun each(configure: Component.() -> Unit) {
		(this as Component).configure()
	}


	@RaptorDsl
	public abstract class Base<Component : RaptorComponent<Component>>(
		override val plugin: RaptorPluginWithConfiguration<*>,
	) : RaptorComponent<Component> {

		@RaptorDsl
		final override val extensions: RaptorComponentExtensionSet = DefaultComponentExtensionSet()


		@RaptorDsl
		@Suppress("UNCHECKED_CAST")
		final override fun each(configure: Component.() -> Unit) {
			(this as Component).configure()
		}
	}
}
