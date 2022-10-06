package io.fluidsonic.raptor.ktor.graph

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.graph.*
import io.fluidsonic.raptor.ktor.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


internal class RaptorGraphSchemaKtorRoutePlugin(
	private val tag: Any? = null,
) : RaptorKtorRoutePlugin {

	override fun RaptorKtorRoutePluginConfigurationEndScope.onConfigurationEnded() {
		val graphConfiguration = require(RaptorGraphPlugin)

		route {
			propertyRegistry.register(Keys.graphSchemaProperty, graphConfiguration.taggedGraph(tag).schema)
		}
	}


	override fun RaptorKtorRoutePluginConfigurationStartScope.onConfigurationStarted() {
		route {
			custom {
				get {
					val schema = checkNotNull(raptorContext[Keys.graphSchemaProperty])

					call.respondText(schema.toString(), ContentType.Text.Plain)
				}
			}
		}
	}


	override fun toString(): String = "graph schema (tag = $tag)"
}


// TODO Require RaptorKtorGraphPlugin to be installed.
@RaptorDsl
public fun RaptorAssemblyQuery<RaptorKtorRouteComponent>.graphSchema(tag: Any? = null) {
	val plugin = RaptorGraphSchemaKtorRoutePlugin(tag = tag)

	each {
		check(extensions[Keys.graphInstalledExtension] != true) {
			"Cannot install multiple graphSchema() handlers or mix it with graph() in the same Ktor route."
		}

		extensions[Keys.graphInstalledExtension] = true

		install(plugin)
	}
}
