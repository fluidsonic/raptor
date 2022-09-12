package io.fluidsonic.raptor


public typealias RaptorFeatureScope = RaptorFeatureConfigurationScope


public interface RaptorFeature {

	public fun RaptorFeatureConfigurationApplicationScope.applyConfiguration(): Unit = Unit
	public fun RaptorFeatureConfigurationScope.completeConfiguration(): Unit = Unit

	public fun RaptorFeatureScope.installed() {
		@Suppress("DEPRECATION")
		beginConfiguration()
	}

	@Deprecated("Renamed to 'RaptorFeatureScope.installed()'.", replaceWith = ReplaceWith("installed()"))
	public fun RaptorFeatureConfigurationScope.beginConfiguration(): Unit = Unit


	public companion object;


	public interface Configurable<out ConfigurationScope : Any> : RaptorFeature {

		public fun RaptorFeatureConfigurationScope.beginConfiguration(action: ConfigurationScope.() -> Unit)
	}
}
