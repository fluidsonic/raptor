package io.fluidsonic.raptor

import io.ktor.routing.*
import org.kodein.di.*


internal class KtorRouteConfig(
	val children: List<KtorRouteConfig>,
	val customConfig: Route.() -> Unit,
	val kodeinModule: Kodein.Module,
	val path: String
)
