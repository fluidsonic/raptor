package io.fluidsonic.raptor

import io.ktor.routing.*


internal class KtorRouteConfiguration(
	val children: List<KtorRouteConfiguration>,
	val customConfigurations: List<RaptorKtorRouteInitializationScope.() -> Unit>,
	val host: String?,
	val path: String,
	val properties: RaptorPropertySet,
	val transactionFactory: RaptorTransactionFactory?,
	val wrapper: (RaptorKtorRouteInitializationScope.(next: Route.() -> Unit) -> Unit)?,
)
