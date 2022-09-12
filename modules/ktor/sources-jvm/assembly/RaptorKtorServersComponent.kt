package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*


public class RaptorKtorServersComponent internal constructor() : RaptorComponent2.Base(), RaptorComponentSet2<RaptorKtorServerComponent> {

	@RaptorDsl
	override val all: RaptorAssemblyQuery2<RaptorKtorServerComponent>
		get() = componentRegistry2.all(RaptorKtorServerComponent.Key).all


	@RaptorDsl
	public fun new(forceEncryptedConnection: Boolean = true): RaptorKtorServerComponent =
		componentRegistry2.register(RaptorKtorServerComponent.Key) { RaptorKtorServerComponent(forceEncryptedConnection = forceEncryptedConnection) }


	@RaptorDsl
	public fun new(forceEncryptedConnection: Boolean = true, configure: RaptorKtorServerComponent.() -> Unit = {}) {
		new(forceEncryptedConnection = forceEncryptedConnection).configure()
	}


	internal object Key : RaptorComponentKey2<RaptorKtorServersComponent> {

		override fun toString() = "servers"
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorKtorServersComponent>.new(
	forceEncryptedConnection: Boolean = true,
): RaptorAssemblyQuery2<RaptorKtorServerComponent> =
	map { it.new(forceEncryptedConnection = forceEncryptedConnection) }


@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorKtorServersComponent>.new(
	forceEncryptedConnection: Boolean = true,
	configure: RaptorKtorServerComponent.() -> Unit = {},
) {
	this {
		new(forceEncryptedConnection = forceEncryptedConnection).configure()
	}
}
