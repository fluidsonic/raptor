package io.fluidsonic.raptor


internal class KtorServerConfiguration(
	val customConfigurations: List<RaptorKtorConfigurationScope.() -> Unit>,
	val rootRouteConfiguration: KtorRouteConfiguration?,
	val transactionFactory: RaptorTransactionFactory // FIXME use
)
