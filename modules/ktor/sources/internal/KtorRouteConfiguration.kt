package io.fluidsonic.raptor

import io.ktor.routing.*


internal class KtorRouteConfiguration(
	val children: List<KtorRouteConfiguration>,
	val customConfig: Route.() -> Unit,
	val path: String,
	val wrapper: (Route.(next: Route.() -> Unit) -> Unit)?
)
