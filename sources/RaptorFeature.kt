package io.fluidsonic.raptor


interface RaptorFeature<ConfigDsl : Any> {

	fun RaptorConfigScope.configure() = Unit
	fun RaptorConfigScope.configure(dslConfig: ConfigDsl.() -> Unit) = Unit // FIXME default impl must call block
	fun RaptorRouteConfigScope.configure() = Unit
	fun RaptorServerConfigScope.configure() = Unit
	fun RaptorTransactionConfigScope.configure() = Unit
}
