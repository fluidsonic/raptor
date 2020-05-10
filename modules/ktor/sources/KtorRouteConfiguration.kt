package io.fluidsonic.raptor

import io.ktor.routing.*


internal class KtorRouteConfiguration(
	val children: List<KtorRouteConfiguration>,
	val customConfigurations: List<RaptorKtorRouteConfigurationScope.() -> Unit>,
	val path: String,
	val transactionFactory: RaptorTransactionFactory?, // FIXME use
	val wrapper: (RaptorKtorRouteConfigurationScope.(next: Route.() -> Unit) -> Unit)?
)
