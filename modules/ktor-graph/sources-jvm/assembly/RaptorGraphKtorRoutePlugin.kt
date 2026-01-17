package io.fluidsonic.raptor.ktor.graph

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.graph.*
import io.fluidsonic.raptor.ktor.*
import io.fluidsonic.raptor.transactions.*
import io.ktor.server.routing.*


internal class RaptorGraphKtorRoutePlugin(
	private val executionHook: RaptorGraphKtorRouteExecutionHook? = null,
	private val tag: Any? = null,
) : RaptorKtorRoutePlugin {

	override fun RaptorKtorRoutePluginConfigurationEndScope.onConfigurationEnded() {
		val graphConfiguration = require(RaptorGraphPlugin)

		route {
			propertyRegistry.register(
				Keys.graphRouteProperty, GraphRoute(
					executionHook = executionHook,
					graph = graphConfiguration.taggedGraph(tag),
				)
			)
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
public fun RaptorAssemblyQuery<RaptorKtorRouteComponent>.graph(
	executionHook: RaptorGraphKtorRouteExecutionHook? = null,
	tag: Any? = null,
) {
	val plugin = RaptorGraphKtorRoutePlugin(executionHook = executionHook, tag = tag)

	each {
		check(extensions[Keys.graphInstalledExtension] != true) {
			"Cannot install multiple graph() handlers or mix it with graphSchema() in the same Ktor route."
		}

		extensions[Keys.graphInstalledExtension] = true

		install(plugin)
	}
}


public data class RaptorKtorGraphRequest(
	val document: GDocument,
	val operationName: String?,
	val query: String,
	val variableValues: Map<String, Any?>,
)


public typealias RaptorGraphKtorRouteExecutionHook =
	suspend RaptorTransactionContext.(request: RaptorKtorGraphRequest, next: suspend (request: RaptorKtorGraphRequest) -> GResult<Map<String, Any?>>) -> GResult<Map<String, Any?>>
