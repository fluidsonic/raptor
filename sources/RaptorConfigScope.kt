package io.fluidsonic.raptor

import org.kodein.di.*


interface RaptorConfigScope {

	fun <ConfigDsl : Any> install(feature: RaptorFeature<ConfigDsl>, config: ConfigDsl.() -> Unit = {})
	fun kodein(config: Kodein.Builder.() -> Unit) // FIXME wrap Kodein API?
	fun server(config: RaptorServerConfigScope.() -> Unit)

	fun onStart(callback: suspend RaptorScope.() -> Unit)
	fun onStop(callback: suspend RaptorScope.() -> Unit)
}
