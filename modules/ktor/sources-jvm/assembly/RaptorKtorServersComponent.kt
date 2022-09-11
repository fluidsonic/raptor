package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*


public class RaptorKtorServersComponent internal constructor() : RaptorComponent2.Base(), RaptorComponentSet2<RaptorKtorServerComponent> {

	@RaptorDsl
	override fun all(configure: RaptorKtorServerComponent.() -> Unit) {
		componentRegistry2.all(RaptorKtorServerComponent.Key, configure)
	}


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
