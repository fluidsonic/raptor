package io.fluidsonic.raptor


@Raptor.Dsl3
class KtorServerFeatureComponent internal constructor(
	internal val globalFeatureSetup: RaptorFeatureSetup,
	internal val serverComponent: KtorServerRaptorComponent
) : RaptorComponent
