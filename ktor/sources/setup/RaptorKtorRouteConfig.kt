package io.fluidsonic.raptor

import io.ktor.routing.*
import org.kodein.di.*


internal class RaptorKtorRouteConfig(
	val children: List<RaptorKtorRouteConfig>,
	val kodeinModule: Kodein.Module,
	val ktorConfig: Route.() -> Unit,
	val path: String
)
