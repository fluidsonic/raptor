package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*


public abstract class RaptorKtorRoutesComponent<Component : RaptorKtorRoutesComponent<Component>> internal constructor() :
	RaptorComponent2.Base<Component>(),
	RaptorComponentSet2<RaptorKtorRouteComponent> {

	@RaptorDsl
	override val all: RaptorAssemblyQuery2<RaptorKtorRouteComponent>
		get() = componentRegistry2.all(RaptorKtorRouteComponent.Key).all


	@RaptorDsl
	public fun new(path: String, host: String? = null): RaptorKtorRouteComponent =
		componentRegistry2.register(RaptorKtorRouteComponent.Key) { RaptorKtorRouteComponent(host = host, path = path) }


	@RaptorDsl
	public fun new(path: String, host: String? = null, configure: RaptorKtorRouteComponent.() -> Unit = {}) {
		new(host = host, path = path).configure()
	}


	public class NonRoot internal constructor() : RaptorKtorRoutesComponent<NonRoot>()


	public class Root internal constructor() : RaptorKtorRoutesComponent<Root>() {

		@RaptorDsl
		public fun new(host: String? = null): RaptorKtorRouteComponent =
			componentRegistry2.register(RaptorKtorRouteComponent.Key) { RaptorKtorRouteComponent(host = host, path = "/") }


		@RaptorDsl
		public fun new(host: String? = null, configure: RaptorKtorRouteComponent.() -> Unit = {}) {
			new(host = host).configure()
		}


		internal object Key : RaptorComponentKey2<Root> {

			override fun toString() = "routes"
		}
	}


	internal object Key : RaptorComponentKey2<NonRoot> {

		override fun toString() = "routes"
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorKtorRoutesComponent<*>>.new(path: String, host: String? = null): RaptorAssemblyQuery2<RaptorKtorRouteComponent> =
	map { it.new(path = path, host = host) }


@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorKtorRoutesComponent<*>>.new(
	path: String,
	host: String? = null,
	configure: RaptorKtorRouteComponent.() -> Unit = {},
) {
	this {
		new(host = host, path = path).configure()
	}
}


@JvmName("rootNew")
@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorKtorRoutesComponent.Root>.new(host: String? = null): RaptorAssemblyQuery2<RaptorKtorRouteComponent> =
	map { it.new(host = host) }


@JvmName("rootNew")
@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorKtorRoutesComponent.Root>.new(
	host: String? = null,
	configure: RaptorKtorRouteComponent.() -> Unit = {},
) {
	this {
		new(host = host).configure()
	}
}
