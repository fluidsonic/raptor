//package io.fluidsonic.raptor.ktor.graph
//
//import io.fluidsonic.raptor.*
//import io.fluidsonic.raptor.ktor.*
//import io.ktor.application.*
//import io.ktor.http.*
//import io.ktor.response.*
//import io.ktor.routing.*
//
//
//public object RaptorGraphKtorFeature : RaptorKtorRouteFeature {
//
//	override fun RaptorKtorRouteFeatureConfigurationEndScope.onConfigurationEnded() {
//		route {
//			propertyRegistry.register(GraphRoute.PropertyKey, componentRegistry2.one(GraphRaptorComponent.Key).toGraphRoute())
//		}
//	}
//
//
//	override fun RaptorKtorRouteFeatureConfigurationStartScope.onConfigurationStarted() {
//		route {
//			custom {
//				get {
//					checkNotNull(raptorContext[GraphRoute.PropertyKey]).handle(call)
//				}
//
//				post {
//					checkNotNull(raptorContext[GraphRoute.PropertyKey]).handle(call)
//				}
//			}
//		}
//	}
//
//
//	override fun toString(): String = "ktor graph"
//}
