package io.fluidsonic.raptor


@Raptor.Dsl3
class KtorRouteFeatureComponent internal constructor(
	globalComponent: RaptorFeatureComponent,
	routeComponent: KtorRouteRaptorComponent,
	serverComponent: KtorServerRaptorComponent
) : RaptorComponent {

	@Raptor.Dsl3
	val global = RaptorComponentConfig.of(globalComponent)


	@Raptor.Dsl3
	val route = RaptorComponentConfig.of(routeComponent)


	@Raptor.Dsl3
	val server = RaptorComponentConfig.of(serverComponent)
}
