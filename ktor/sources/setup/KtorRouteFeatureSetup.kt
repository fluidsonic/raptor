package io.fluidsonic.raptor


typealias KtorRouteFeatureSetup = RaptorComponentScope<KtorRouteFeatureComponent>


@Raptor.Dsl3
val KtorRouteFeatureSetup.global
	get() = raptorComponentSelection.map { component.globalFeatureSetup }


@Raptor.Dsl3
val KtorRouteFeatureSetup.route
	get() = raptorComponentSelection.map { registry.configureSingle(component.routeComponent) }
// FIXME crash here because registry points to children but we query the component itself which is in the parent registry!


@Raptor.Dsl3
val KtorRouteFeatureSetup.server
	get() = raptorComponentSelection.map { registry.configureSingle(component.serverComponent) }
