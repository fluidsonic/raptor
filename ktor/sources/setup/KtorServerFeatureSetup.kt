package io.fluidsonic.raptor


typealias KtorServerFeatureSetup = RaptorComponentScope<KtorServerFeatureComponent>


@Raptor.Dsl3
val KtorServerFeatureSetup.global
	get() = raptorComponentSelection.map { component.globalFeatureSetup }


@Raptor.Dsl3
val KtorServerFeatureSetup.server
	get() = raptorComponentSelection.map { registry.configureSingle(component.serverComponent) }
