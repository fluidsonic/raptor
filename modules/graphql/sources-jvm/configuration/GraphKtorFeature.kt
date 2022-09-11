package io.fluidsonic.raptor

import io.fluidsonic.raptor.ktor.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*


public object GraphKtorFeature : RaptorKtorRouteFeature {

	override fun RaptorKtorRouteFeatureConfigurationEndScope.onConfigurationEnded() {
		route {
			propertyRegistry.register(GraphRoute.PropertyKey, componentRegistry2.one(GraphRaptorComponent.Key).toGraphRoute())
		}
	}


	override fun RaptorKtorRouteFeatureConfigurationStartScope.onConfigurationStarted() {
		route {
			custom {
				get {
					checkNotNull(raptorContext[GraphRoute.PropertyKey]).handle(call)
				}

				post {
					checkNotNull(raptorContext[GraphRoute.PropertyKey]).handle(call)
				}
			}
		}
	}
}


// FIXME
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
		.also { componentRegistry2.register(GraphRaptorComponent.Key, it) }
		.also(configuration)

	if (provideSchema)
		routes.new("schema") {
			custom {
				get {
					val schema = raptorContext[GraphRoute.PropertyKey]!!.system.schema

					call.respondText(schema.toString(), ContentType.Text.Plain)
				}
			}
		}
}
