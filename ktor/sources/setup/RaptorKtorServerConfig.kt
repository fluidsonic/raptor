package io.fluidsonic.raptor

import io.ktor.application.*
import org.kodein.di.*


internal class RaptorKtorServerConfig(
	val kodeinModule: Kodein.Module,
	val ktorApplicationConfig: Application.() -> Unit,
	val routingConfig: RaptorKtorRouteConfig?
)
