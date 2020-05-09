package io.fluidsonic.raptor


@RaptorDsl
interface RaptorFeatureInstallationScope : RaptorFeatureInstallationTarget {

	@RaptorDsl
	val registry: RaptorComponentRegistry // FIXME we use registry.xyz() in InstallationScope but direct fn in FinalizationScope!


	companion object
}
