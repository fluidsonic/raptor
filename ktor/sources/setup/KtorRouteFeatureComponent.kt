package io.fluidsonic.raptor


@Raptor.Dsl3
class KtorRouteFeatureComponent internal constructor(
	internal val globalFeatureSetup: RaptorFeatureSetup,
	internal val routeComponent: KtorRouteRaptorComponent,
	internal val serverComponent: KtorServerRaptorComponent
) : RaptorComponent
