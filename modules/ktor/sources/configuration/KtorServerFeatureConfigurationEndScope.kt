package io.fluidsonic.raptor


interface KtorServerFeatureConfigurationEndScope {

	@RaptorDsl
	fun global(configuration: RaptorTopLevelConfigurationScope.() -> Unit)

	@RaptorDsl
	fun server(configuration: ServerScope.() -> Unit)


	interface ServerScope {

		@RaptorDsl
		val componentRegistry: RaptorComponentRegistry

		@RaptorDsl
		val propertyRegistry: RaptorPropertyRegistry // FIXME support
	}
}
