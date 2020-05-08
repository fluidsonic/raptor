package io.fluidsonic.raptor


@Raptor.Dsl3
class KtorRouteFeatureComponent internal constructor(
	globalComponent: RaptorFeatureComponent,
	routeComponent: KtorRouteRaptorComponent,
	serverComponent: KtorServerRaptorComponent
) : RaptorComponent {

	@Raptor.Dsl3
	val global = RaptorComponentSet.of(globalComponent)


	@Raptor.Dsl3
	val route = RaptorComponentSet.of(routeComponent)


	@Raptor.Dsl3
	val server = RaptorComponentSet.of(serverComponent)
}
