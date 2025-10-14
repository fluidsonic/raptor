package io.fluidsonic.raptor.ktor.graph

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.graph.*
import io.fluidsonic.raptor.ktor.*
import io.ktor.server.routing.*


internal class RaptorGraphKtorRoutePlugin(
	private val tag: Any? = null,
) : RaptorKtorRoutePlugin {

	override fun RaptorKtorRoutePluginConfigurationEndScope.onConfigurationEnded() {
		val graphConfiguration = require(RaptorGraphPlugin)

		route {
			propertyRegistry.register(Keys.graphRouteProperty, GraphRoute(graphConfiguration.taggedGraph(tag)))
		}
	}


	override fun RaptorKtorRoutePluginConfigurationStartScope.onConfigurationStarted() {
		route {
			custom {
				get {
					checkNotNull(raptorContext[Keys.graphRouteProperty]).handle(call)
				}
				post {
					checkNotNull(raptorContext[Keys.graphRouteProperty]).handle(call)
				}
			}
		}
	}


	override fun toString(): String = "graph (tag = $tag)"
}


// TODO Require RaptorKtorGraphPlugin to be installed.
@RaptorDsl
public fun RaptorAssemblyQuery<RaptorKtorRouteComponent>.graph(tag: Any? = null) {
	val plugin = RaptorGraphKtorRoutePlugin(tag = tag)

	each {
		check(extensions[Keys.graphInstalledExtension] != true) {
			"Cannot install multiple graph() handlers or mix it with graphSchema() in the same Ktor route."
		}

		extensions[Keys.graphInstalledExtension] = true

		install(plugin)
	}
}
