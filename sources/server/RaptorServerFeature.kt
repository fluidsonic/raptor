package io.fluidsonic.raptor


interface RaptorServerFeature<ConfigDsl : Any> {

	fun RaptorRouteConfigScope.configure() = Unit
	fun RaptorServerConfigScope.configure(dslConfig: ConfigDsl.() -> Unit) = Unit
	fun RaptorTransactionConfigScope.configure() = Unit
}
