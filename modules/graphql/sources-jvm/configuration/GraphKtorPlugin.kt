package io.fluidsonic.raptor

import io.fluidsonic.raptor.ktor.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


public object GraphKtorPlugin : RaptorKtorRoutePlugin {

	override fun RaptorKtorRoutePluginConfigurationEndScope.onConfigurationEnded() {
		route {
			propertyRegistry.register(GraphRoute.propertyKey, componentRegistry.one(GraphRaptorComponent.key).toGraphRoute())
		}
	}


	override fun RaptorKtorRoutePluginConfigurationStartScope.onConfigurationStarted() {
		route {
			custom {
				get {
					checkNotNull(raptorContext[GraphRoute.propertyKey]).handle(call)
				}

				post {
					checkNotNull(raptorContext[GraphRoute.propertyKey]).handle(call)
				}
			}
		}
	}
}


// TODO
//@RaptorDsl
//public val RaptorTopLevelConfigurationScope.graphs: RaptorComponentSet2<GraphRaptorComponent>
//	get() = ktor.servers.all.routes.new(recursive = true).withComponentAuthoring {
//		map {
//			componentRegistry.configure(GraphRaptorComponent.Key)
//		}
//	}


@RaptorDsl
public fun RaptorKtorRouteComponent.newGraph(provideSchema: Boolean = false, configuration: GraphRaptorComponent.() -> Unit = {}) {
	GraphRaptorComponent()
		.also { componentRegistry.register(GraphRaptorComponent.key, it) }
		.also(configuration)

	if (provideSchema)
		routes.new("schema") {
			custom {
				get {
					val schema = raptorContext[GraphRoute.propertyKey]!!.system.schema

					call.respondText(schema.toString(), ContentType.Text.Plain)
				}
			}
		}
}
