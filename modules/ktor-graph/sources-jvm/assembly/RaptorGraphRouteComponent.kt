package io.fluidsonic.raptor.ktor.graph

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.graph.*
import io.fluidsonic.raptor.ktor.*
import io.ktor.server.application.*
import io.ktor.server.routing.*


public class RaptorGraphRouteComponent internal constructor(
	private val route: RaptorKtorRouteComponent,
	private val tag: Any? = null,
) : RaptorComponent.Base<RaptorGraphRouteComponent>() {

	override fun RaptorComponentConfigurationStartScope.onConfigurationStarted() {
		route.custom {
			get {
				val route = checkNotNull(raptorContext[propertyKey])
				route.handle(call)
			}

			post {
				val route = checkNotNull(raptorContext[propertyKey])
				route.handle(call)
			}
		}
	}


	override fun RaptorComponentConfigurationEndScope<RaptorGraphRouteComponent>.onConfigurationEnded() {
		val graph = checkNotNull(graph(tag)) { if (tag != null) "Cannot find graph with tag: $tag" else "Cannot find any graph" }
		// FIXME
//		propertyRegistry.register(PropertyKey, GraphRoute(graph))
	}


	internal companion object {

		val key = RaptorComponentKey<RaptorGraphRouteComponent>("graph")
		val propertyKey = RaptorPropertyKey<GraphRoute>("graph")
	}
}


@RaptorDsl
public fun RaptorKtorRouteComponent.graph(tag: Any? = null): RaptorGraphRouteComponent =
	componentRegistry.oneOrRegister(RaptorGraphRouteComponent.key) { RaptorGraphRouteComponent(route = this, tag = tag) }


@RaptorDsl
public fun RaptorKtorRouteComponent.graph(tag: Any? = null, configure: RaptorGraphRouteComponent.() -> Unit): RaptorGraphRouteComponent =
	graph(tag = tag).apply(configure)

// FIXME


//package io.fluidsonic.raptor.ktor.graph
//
//import io.fluidsonic.raptor.*
//import io.fluidsonic.raptor.graph.*
//import io.ktor.http.*
//
////provideSchema: Boolean = false
//
//public class RaptorGraphRouteComponent internal constructor(
//	private val route: RaptorKtorRouteComponent,
//) : RaptorComponent2.Base() {
//
//	internal fun finalize(): RaptorGraph =
//		checkNotNull(graph)
//
//
//	@RaptorDsl
//	public val schemaRoutes:
//	@RaptorDsl
//	public fun schemaRoute(vararg definitions: RaptorGraphDefinition) {
//		definitions(definitions.asIterable())
//	}
//
//
//	@RaptorDsl
//	public fun definitions(definitions: Iterable<RaptorGraphDefinition>) {
//		this.definitions += definitions
//	}
//
//
//	@RaptorDsl
//	public fun includeDefaultDefinitions() {
//		if (includesDefaultDefinitions)
//			return
//
//		includesDefaultDefinitions = true
//
//		definitions(RaptorGraphDefaults.definitions)
//	}
//
//
//	override fun RaptorComponentConfigurationEndScope2.onConfigurationEnded() {
//		graph = GraphSystemDefinitionBuilder.build(definitions)
//			.let(GraphTypeSystemBuilder::build)
//			.let { GraphSystemBuilder.build(tags = tags(this@RaptorGraphComponent), typeSystem = it) }
//	}
//
//
//	internal object Key : RaptorComponentKey2<RaptorGraphComponent> {
//
//		override fun toString() = "graph"
//	}
//}
//
//
//
//@RaptorDsl
//public val RaptorKtorRouteComponent.graph
//	get() = componentRegistry.oneOrRegister(RaptorGraphRouteComponent.Key) { RaptorGraphRouteComponent(route = this) }
//
//
////if (provideSchema) {
////	newRoute("schema") {
////		custom {
////			get {
////				val schema = raptorContext[GraphRoute.PropertyKey]!!.system.schema
////
////				call.respondText(schema.toString(), ContentType.Text.Plain)
////			}
////		}
////	}
////}
