package io.fluidsonic.raptor

import io.ktor.application.*
import org.kodein.di.*


interface RaptorServerConfigScope {

	fun <ConfigDsl : Any> install(feature: RaptorServerFeature<ConfigDsl>, config: ConfigDsl.() -> Unit = {})
	fun kodein(config: Kodein.Builder.() -> Unit)
	fun ktor(config: Application.() -> Unit)
	fun route(config: RaptorRouteConfigScope.() -> Unit) = route("", config = config)
	fun route(path: String, config: RaptorRouteConfigScope.() -> Unit)
}
