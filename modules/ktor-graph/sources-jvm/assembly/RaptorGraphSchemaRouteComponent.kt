package io.fluidsonic.raptor.ktor.graph

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.graph.*
import io.fluidsonic.raptor.ktor.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


public class RaptorGraphSchemaRouteComponent internal constructor(
	private val route: RaptorKtorRouteComponent,
	private val tag: Any? = null,
) : RaptorComponent.Base<RaptorGraphSchemaRouteComponent>(RaptorKtorGraphPlugin) {

	override fun RaptorComponentConfigurationStartScope.onConfigurationStarted() {
		route.custom {
			get {
				val schema = checkNotNull(raptorContext[propertyKey])
				call.respondText(schema.toString(), ContentType.Text.Plain)
			}
		}
	}


	override fun RaptorComponentConfigurationEndScope<RaptorGraphSchemaRouteComponent>.onConfigurationEnded() {
		// FIXME Won't work unless we make the property registry and the context hierarchical.
		val graph = checkNotNull(graph(tag)) { if (tag != null) "Cannot find graph with tag: $tag" else "Cannot find any graph" }
		propertyRegistry.register(propertyKey, graph.schema)
	}


	internal companion object {

		val key = RaptorComponentKey<RaptorGraphSchemaRouteComponent>("graph schema")
		val propertyKey = RaptorPropertyKey<GSchema>("graph schema")
	}
}


@RaptorDsl
public fun RaptorKtorRouteComponent.graphSchema(tag: Any? = null): RaptorGraphSchemaRouteComponent =
	componentRegistry.oneOrRegister(RaptorGraphSchemaRouteComponent.key) { RaptorGraphSchemaRouteComponent(route = this, tag = tag) }


@RaptorDsl
public fun RaptorKtorRouteComponent.graphSchema(tag: Any? = null, configure: RaptorGraphSchemaRouteComponent.() -> Unit): RaptorGraphSchemaRouteComponent =
	graphSchema(tag = tag).apply(configure)
