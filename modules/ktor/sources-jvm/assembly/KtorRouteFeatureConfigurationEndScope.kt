package io.fluidsonic.raptor


public interface KtorRouteFeatureConfigurationEndScope {

	@RaptorDsl
	public fun global(configuration: RaptorTopLevelConfigurationScope.() -> Unit)

	@RaptorDsl
	public fun route(configuration: RouteScope.() -> Unit)

	@RaptorDsl
	public fun server(configuration: ServerScope.() -> Unit)


	public interface RouteScope {

		@RaptorDsl
		public val componentRegistry: RaptorComponentRegistry

		@RaptorDsl
		public val propertyRegistry: RaptorPropertyRegistry // FIXME support
	}


	public interface ServerScope {

		@RaptorDsl
		public val componentRegistry: RaptorComponentRegistry

		@RaptorDsl
		public val propertyRegistry: RaptorPropertyRegistry // FIXME support
	}
}
