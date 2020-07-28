package io.fluidsonic.raptor


interface KtorRouteFeatureConfigurationEndScope {

	@RaptorDsl
	fun global(configuration: RaptorTopLevelConfigurationScope.() -> Unit)

	@RaptorDsl
	fun route(configuration: RouteScope.() -> Unit)

	@RaptorDsl
	fun server(configuration: ServerScope.() -> Unit)


	interface RouteScope {

		@RaptorDsl
		val componentRegistry: RaptorComponentRegistry

		@RaptorDsl
		val propertyRegistry: RaptorPropertyRegistry // FIXME support
	}


	interface ServerScope {

		@RaptorDsl
		val componentRegistry: RaptorComponentRegistry

		@RaptorDsl
		val propertyRegistry: RaptorPropertyRegistry // FIXME support
	}
}
