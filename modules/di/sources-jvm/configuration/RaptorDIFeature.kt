package io.fluidsonic.raptor


public object RaptorDIFeature : RaptorFeature {

	override val id: RaptorFeatureId = raptorDIFeatureId


	override fun RaptorFeatureConfigurationScope.beginConfiguration() {
		componentRegistry.register(RootDIRaptorComponent.Key, RootDIRaptorComponent())
	}
}


public const val raptorDIFeatureId: RaptorFeatureId = "raptor.di"


@RaptorDsl
public fun RaptorTopLevelConfigurationScope.di(configuration: RaptorDIBuilder.() -> Unit) {
	componentRegistry.configure(RootDIRaptorComponent.Key) {
		builder.apply(configuration)
	}
}
