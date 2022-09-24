package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*
import io.ktor.server.routing.*


internal class KtorRouteConfiguration(
	val children: Collection<KtorRouteConfiguration>,
	val customConfigurations: Collection<RaptorKtorRouteInitializationScope.() -> Unit>,
	val host: String?,
	val path: String,
	val properties: RaptorPropertySet,
	val transactionFactory: RaptorTransactionFactory?,
	val wrapper: (RaptorKtorRouteInitializationScope.(next: Route.() -> Unit) -> Unit)?,
)
