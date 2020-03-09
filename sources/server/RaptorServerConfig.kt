package io.fluidsonic.raptor

import io.ktor.application.*
import org.kodein.di.*


internal class RaptorServerConfig(
	val kodeinModule: Kodein.Module,
	val ktorConfig: Application.() -> Unit,
	val rootRouteConfig: RaptorRouteConfig?
)
