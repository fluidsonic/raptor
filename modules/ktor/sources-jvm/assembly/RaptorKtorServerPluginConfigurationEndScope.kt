package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*


public interface RaptorKtorServerPluginConfigurationEndScope {

	@RaptorDsl
	public fun server(configuration: ServerScope.() -> Unit)


	@RaptorDsl
	public interface ServerScope {

		@RaptorDsl
		public val componentRegistry: RaptorComponentRegistry

		@RaptorDsl
		public val propertyRegistry: RaptorPropertyRegistry
	}
}
