package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*


public object RaptorGraphKtorFeature : KtorRouteFeature {

	override fun KtorRouteFeatureConfigurationEndScope.onConfigurationEnded() {
		route {
			propertyRegistry.register(GraphRoute.PropertyKey, componentRegistry.one(GraphRaptorComponent.Key).toGraphRoute())
		}
	}


	override fun KtorRouteFeatureConfigurationStartScope.onConfigurationStarted() {
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


@RaptorDsl
public val RaptorTopLevelConfigurationScope.graphs: RaptorComponentSet<GraphRaptorComponent>
	get() = ktor.servers.routes(recursive = true).withComponentAuthoring {
		map {
			componentRegistry.configure(GraphRaptorComponent.Key)
		}
	}


@RaptorDsl
public fun KtorRouteRaptorComponent.newGraph(provideSchema: Boolean = false, configuration: GraphRaptorComponent.() -> Unit = {}) {
	GraphRaptorComponent()
		.also { componentRegistry.register(GraphRaptorComponent.Key, it) }
		.also(configuration)

	if (provideSchema) {
		newRoute("schema") {
			custom {
				get {
					val schema = raptorContext[GraphRoute.PropertyKey]!!.system.schema

					call.respondText(schema.toString(), ContentType.Text.Plain)
				}
			}
		}
	}
}
