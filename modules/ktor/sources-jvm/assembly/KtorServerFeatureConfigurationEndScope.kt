package io.fluidsonic.raptor


public interface KtorServerFeatureConfigurationEndScope {

	@RaptorDsl
	public fun global(configuration: RaptorTopLevelConfigurationScope.() -> Unit)

	@RaptorDsl
	public fun server(configuration: ServerScope.() -> Unit)


	public interface ServerScope {

		@RaptorDsl
		public val componentRegistry: RaptorComponentRegistry

		@RaptorDsl
		public val propertyRegistry: RaptorPropertyRegistry // FIXME support
	}
}
