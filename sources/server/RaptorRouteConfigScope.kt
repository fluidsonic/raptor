package io.fluidsonic.raptor

import io.ktor.routing.*
import org.kodein.di.*


interface RaptorRouteConfigScope {

	fun <ConfigDsl : Any> install(feature: RaptorRouteFeature<ConfigDsl>, config: ConfigDsl.() -> Unit = {})
	fun kodein(config: Kodein.Builder.() -> Unit)
	fun ktor(config: Route.() -> Unit)
	fun route(path: String, config: RaptorRouteConfigScope.() -> Unit)
}
