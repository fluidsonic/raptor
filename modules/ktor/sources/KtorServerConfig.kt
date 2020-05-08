package io.fluidsonic.raptor

import io.ktor.application.*
import org.kodein.di.*


internal class KtorServerConfig(
	val customConfig: Application.() -> Unit,
	val kodeinModule: Kodein.Module,
	val routingConfig: KtorRouteConfig?
)
