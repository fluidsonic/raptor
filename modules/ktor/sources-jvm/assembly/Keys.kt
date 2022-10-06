package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*
import io.ktor.util.*


internal object Keys {

	val ktorComponent = RaptorComponentKey<RaptorKtorComponent>("ktor")
	val ktorProperty = RaptorPropertyKey<RaptorKtorInternal>("ktor")
	val rootRoutesComponent = RaptorComponentKey<RaptorKtorRoutesComponent.Root>("routes")
	val routeComponent = RaptorComponentKey<RaptorKtorRouteComponent>("route")
	val routesComponent = RaptorComponentKey<RaptorKtorRoutesComponent.NonRoot>("routes")
	val serverComponent = RaptorComponentKey<RaptorKtorServerComponent>("server")
	val serverKtorAttribute = AttributeKey<RaptorKtorServerInternal>("Raptor: server")
	val serversComponent = RaptorComponentKey<RaptorKtorServersComponent>("servers")
}
