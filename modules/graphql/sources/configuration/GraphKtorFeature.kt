package io.fluidsonic.raptor

import io.ktor.application.*
import io.ktor.routing.*


object GraphKtorFeature : KtorRouteFeature {

	override fun KtorRouteFeatureConfigurationEndScope.onConfigurationEnded() {
		route {
			propertyRegistry.register(GraphRoute.PropertyKey, componentRegistry.one(GraphRaptorComponent.Key).toGraphRoute())
		}
	}


	override fun KtorRouteFeatureConfigurationStartScope.onConfigurationStarted() {
		route {
			custom {
				get {
					val r = raptorContext
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
val RaptorTopLevelConfigurationScope.graphs: RaptorComponentSet<GraphRaptorComponent>
	get() = ktor.servers.routes(recursive = true).withComponentAuthoring {
		map {
			componentRegistry.configure(GraphRaptorComponent.Key)
		}
	}


@RaptorDsl
fun KtorRouteRaptorComponent.newGraph(configuration: GraphRaptorComponent.() -> Unit = {}) {
	GraphRaptorComponent()
		.also { componentRegistry.register(GraphRaptorComponent.Key, it) }
		.also(configuration)
}
