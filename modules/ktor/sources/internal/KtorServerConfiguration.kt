package io.fluidsonic.raptor


internal class KtorServerConfiguration(
	val customConfigurations: List<RaptorKtorConfigurationScope.() -> Unit>,
	val routingConfiguration: KtorRouteConfiguration?
)
