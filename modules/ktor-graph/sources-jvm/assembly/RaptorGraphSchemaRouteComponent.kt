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
) : RaptorComponent2.Base<RaptorGraphSchemaRouteComponent>() {

	override fun RaptorComponentConfigurationStartScope2.onConfigurationStarted() {
		route.custom {
			get {
				val schema = checkNotNull(raptorContext[PropertyKey])
				call.respondText(schema.toString(), ContentType.Text.Plain)
			}
		}
	}


	override fun RaptorComponentConfigurationEndScope2.onConfigurationEnded() {
		// FIXME Won't work unless we make the property registry and the context hierarchical.
		val graph = checkNotNull(graph(tag)) { if (tag != null) "Cannot find graph with tag: $tag" else "Cannot find any graph" }
		propertyRegistry.register(PropertyKey, graph.schema)
	}


	internal object Key : RaptorComponentKey2<RaptorGraphSchemaRouteComponent> {

		override fun toString() = "graph schema"
	}


	private object PropertyKey : RaptorPropertyKey<GSchema> {

		override fun toString() = "graph schema"
	}
}


@RaptorDsl
public fun RaptorKtorRouteComponent.graphSchema(tag: Any? = null): RaptorGraphSchemaRouteComponent =
	componentRegistry2.oneOrRegister(RaptorGraphSchemaRouteComponent.Key) { RaptorGraphSchemaRouteComponent(route = this, tag = tag) }


@RaptorDsl
public fun RaptorKtorRouteComponent.graphSchema(tag: Any? = null, configure: RaptorGraphSchemaRouteComponent.() -> Unit): RaptorGraphSchemaRouteComponent =
	graphSchema(tag = tag).apply(configure)
