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
//	get() = componentRegistry2.oneOrRegister(RaptorGraphRouteComponent.Key) { RaptorGraphRouteComponent(route = this) }
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
