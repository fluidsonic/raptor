package io.fluidsonic.raptor


interface RaptorRouteFeature<ConfigDsl : Any> {

	fun RaptorRouteConfigScope.configure() = Unit
	fun RaptorRouteConfigScope.configure(dslConfig: ConfigDsl.() -> Unit) = Unit
	fun RaptorTransactionConfigScope.configure() = Unit
}
