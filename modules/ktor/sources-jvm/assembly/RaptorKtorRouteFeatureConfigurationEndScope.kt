package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*


public interface RaptorKtorRouteFeatureConfigurationEndScope {

	@RaptorDsl
	public fun route(configuration: RouteScope.() -> Unit)


	public interface RouteScope {

		@RaptorDsl
		public val componentRegistry: RaptorComponentRegistry

		@RaptorDsl
		public val propertyRegistry: RaptorPropertyRegistry
	}
}
