package io.fluidsonic.raptor


interface RaptorFeature : RaptorConfigurableFeature<RaptorComponent> {

	fun RaptorFeatureFinalizationScope.finalize() = Unit
	fun RaptorFeatureInstallationScope.install()


	@Deprecated("Use .finalize()", level = DeprecationLevel.HIDDEN, replaceWith = ReplaceWith("finalize()"))
	override fun RaptorFeatureFinalizationScope.finalizeConfigurable() {
		finalize()
	}


	@Deprecated("Use .install()", level = DeprecationLevel.HIDDEN, replaceWith = ReplaceWith("install()"))
	override fun RaptorFeatureInstallationScope.installConfigurable(): RaptorComponentKey<RaptorComponent> {
		install()

		return DefaultRaptorFeatureRootComponent.Key(feature = this@RaptorFeature)
			.also { componentRegistry.register(it, DefaultRaptorFeatureRootComponent()) }
	}


	companion object
}
