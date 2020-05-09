package io.fluidsonic.raptor


interface RaptorFeature : RaptorConfigurableFeature<RaptorComponent.Simple> {

	fun RaptorFeatureFinalizationScope.finalize() = Unit
	fun RaptorFeatureInstallationScope.install()


	@Deprecated("Use .finalize()", level = DeprecationLevel.HIDDEN, replaceWith = ReplaceWith("finalize()"))
	override fun RaptorFeatureFinalizationScope.finalizeConfigurable(rootComponent: RaptorComponent.Simple) {
		finalize()
	}


	@Deprecated("Use .install()", level = DeprecationLevel.HIDDEN, replaceWith = ReplaceWith("install()"))
	override fun RaptorFeatureInstallationScope.installConfigurable(): RaptorComponent.Simple {
		install()

		return RaptorComponent.Simple()
	}


	companion object
}
