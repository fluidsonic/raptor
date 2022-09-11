package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*


public class RaptorKtorRoutesComponent internal constructor() : RaptorComponent2.Base(), RaptorComponentSet2<RaptorKtorRouteComponent> {

	@RaptorDsl
	override fun all(configure: RaptorKtorRouteComponent.() -> Unit) {
		componentRegistry2.all(RaptorKtorRouteComponent.Key, configure)
	}


	@RaptorDsl
	public fun new(path: String, host: String? = null): RaptorKtorRouteComponent =
		componentRegistry2.register(RaptorKtorRouteComponent.Key) { RaptorKtorRouteComponent(host = host, path = path) }


	@RaptorDsl
	public fun new(path: String, host: String? = null, configure: RaptorKtorRouteComponent.() -> Unit = {}) {
		new(host = host, path = path).configure()
	}


	internal object Key : RaptorComponentKey2<RaptorKtorRoutesComponent> {

		override fun toString() = "routes"
	}
}
