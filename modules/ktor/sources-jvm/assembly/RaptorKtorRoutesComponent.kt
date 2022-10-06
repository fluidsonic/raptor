package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*


public abstract class RaptorKtorRoutesComponent<Component : RaptorKtorRoutesComponent<Component>> internal constructor() :
	RaptorComponent.Base<Component>(RaptorKtorPlugin),
	RaptorComponentSet<RaptorKtorRouteComponent> {

	@RaptorDsl
	override val all: RaptorAssemblyQuery<RaptorKtorRouteComponent>
		get() = componentRegistry.all(Keys.routeComponent).all


	internal fun complete(): Collection<KtorRouteConfiguration> =
		componentRegistry.many(Keys.routeComponent).map { it.complete() }


	@RaptorDsl
	public fun new(path: String, host: String? = null): RaptorKtorRouteComponent =
		componentRegistry.register(Keys.routeComponent) { RaptorKtorRouteComponent(host = host, path = path) }


	@RaptorDsl
	public fun new(path: String, host: String? = null, configure: RaptorKtorRouteComponent.() -> Unit = {}) {
		new(host = host, path = path).configure()
	}


	public class NonRoot internal constructor() : RaptorKtorRoutesComponent<NonRoot>()


	public class Root internal constructor() : RaptorKtorRoutesComponent<Root>() {

		@RaptorDsl
		public fun new(host: String? = null): RaptorKtorRouteComponent =
			componentRegistry.register(Keys.routeComponent) { RaptorKtorRouteComponent(host = host, path = "/") }


		@RaptorDsl
		public fun new(host: String? = null, configure: RaptorKtorRouteComponent.() -> Unit = {}) {
			new(host = host).configure()
		}
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorKtorRoutesComponent<*>>.new(path: String, host: String? = null): RaptorAssemblyQuery<RaptorKtorRouteComponent> =
	map { it.new(path = path, host = host) }


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorKtorRoutesComponent<*>>.new(
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
public fun RaptorAssemblyQuery<RaptorKtorRoutesComponent.Root>.new(host: String? = null): RaptorAssemblyQuery<RaptorKtorRouteComponent> =
	map { it.new(host = host) }


@JvmName("rootNew")
@RaptorDsl
public fun RaptorAssemblyQuery<RaptorKtorRoutesComponent.Root>.new(
	host: String? = null,
	configure: RaptorKtorRouteComponent.() -> Unit = {},
) {
	this {
		new(host = host).configure()
	}
}
