package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*


public interface RaptorKtorServerFeatureConfigurationEndScope {

	@RaptorDsl
	public fun server(configuration: ServerScope.() -> Unit)


	@RaptorDsl
	public interface ServerScope {

		@RaptorDsl
		public val componentRegistry2: RaptorComponentRegistry2

		@RaptorDsl
		public val propertyRegistry: RaptorPropertyRegistry
	}
}
