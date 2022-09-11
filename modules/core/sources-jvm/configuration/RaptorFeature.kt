package io.fluidsonic.raptor


public interface RaptorFeature {

	public fun RaptorFeatureConfigurationApplicationScope.applyConfiguration(): Unit = Unit
	public fun RaptorFeatureConfigurationScope.beginConfiguration(): Unit = Unit
	public fun RaptorFeatureConfigurationScope.completeConfiguration(): Unit = Unit


	public companion object;


	public interface Configurable<out ConfigurationScope : Any> : RaptorFeature {

		public fun RaptorFeatureConfigurationScope.beginConfiguration(action: ConfigurationScope.() -> Unit)
	}
}
