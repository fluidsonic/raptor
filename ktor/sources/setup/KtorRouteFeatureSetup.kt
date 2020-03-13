package io.fluidsonic.raptor


typealias KtorRouteFeatureSetup = RaptorComponentScope<KtorRouteFeatureComponent>


@Raptor.Dsl3
val KtorRouteFeatureSetup.global
	get() = raptorComponentSelection.map { component.globalFeatureSetup }


@Raptor.Dsl3
val KtorRouteFeatureSetup.route
	get() = raptorComponentSelection.map { containingRegistry.parent!!.configureSingle(component.routeComponent) }


@Raptor.Dsl3
val KtorRouteFeatureSetup.server
	get() = raptorComponentSelection.map { containingRegistry.parent!!.configureSingle(component.serverComponent) }
