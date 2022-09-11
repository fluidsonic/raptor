package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*


public class RaptorKtorComponent internal constructor() : RaptorComponent2.Base() {

	override fun RaptorComponentConfigurationEndScope2.onConfigurationEnded() {
		val servers = componentRegistry2.oneOrNull(RaptorKtorServersComponent.Key)
			?.componentRegistry2
			?.many(RaptorKtorServerComponent.Key)
			?.map { serverComponent ->
				with(serverComponent) {
					toServerConfigurations()
				}
			}
			.orEmpty()

		propertyRegistry.register(DefaultRaptorKtor.PropertyKey, DefaultRaptorKtor(
			configuration = KtorConfiguration(servers = servers),
			context = lazyContext,
		))
	}


	@RaptorDsl
	public val servers: RaptorKtorServersComponent
		get() = componentRegistry2.oneOrRegister(RaptorKtorServersComponent.Key, ::RaptorKtorServersComponent)


	internal object Key : RaptorComponentKey2<RaptorKtorComponent> {

		override fun toString() = "ktor"
	}
}
