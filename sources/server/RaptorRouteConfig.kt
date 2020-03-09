package io.fluidsonic.raptor

import io.ktor.routing.*
import org.kodein.di.*


internal class RaptorRouteConfig(
	val children: List<RaptorRouteConfig>,
	val kodeinModule: Kodein.Module,
	val ktorConfig: Route.() -> Unit,
	val path: String
)
