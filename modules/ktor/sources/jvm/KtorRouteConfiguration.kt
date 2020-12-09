package io.fluidsonic.raptor

import io.ktor.routing.*


internal class KtorRouteConfiguration(
	val children: List<KtorRouteConfiguration>,
	val customConfigurations: List<RaptorKtorRouteConfigurationScope.() -> Unit>,
	val path: String,
	val properties: RaptorPropertySet,
	val transactionFactory: RaptorTransactionFactory?,
	val wrapper: (RaptorKtorRouteConfigurationScope.(next: Route.() -> Unit) -> Unit)?,
)
