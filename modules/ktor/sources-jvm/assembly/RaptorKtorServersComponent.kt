package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*


private val serverComponentKey = RaptorComponentKey<RaptorKtorServerComponent>("server")


public class RaptorKtorServersComponent internal constructor() :
	RaptorComponent.Base<RaptorKtorServersComponent>(RaptorKtorPlugin),
	RaptorComponentSet<RaptorKtorServerComponent> {

	@RaptorDsl
	override val all: RaptorAssemblyQuery<RaptorKtorServerComponent>
		get() = componentRegistry.all(serverComponentKey).all


	internal fun complete(): Collection<KtorServerConfiguration> =
		componentRegistry.many(serverComponentKey).map { it.complete() }


	@RaptorDsl
	public fun new(forceEncryptedConnection: Boolean = true): RaptorKtorServerComponent =
		componentRegistry.register(serverComponentKey) { RaptorKtorServerComponent(forceEncryptedConnection = forceEncryptedConnection) }


	@RaptorDsl
	public fun new(forceEncryptedConnection: Boolean = true, configure: RaptorKtorServerComponent.() -> Unit = {}) {
		new(forceEncryptedConnection = forceEncryptedConnection).configure()
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorKtorServersComponent>.new(
	forceEncryptedConnection: Boolean = true,
): RaptorAssemblyQuery<RaptorKtorServerComponent> =
	map { it.new(forceEncryptedConnection = forceEncryptedConnection) }


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorKtorServersComponent>.new(
	forceEncryptedConnection: Boolean = true,
	configure: RaptorKtorServerComponent.() -> Unit = {},
) {
	this {
		new(forceEncryptedConnection = forceEncryptedConnection).configure()
	}
}
