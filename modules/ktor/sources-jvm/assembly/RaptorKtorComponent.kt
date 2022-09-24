package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*


private val serversComponentKey = RaptorComponentKey<RaptorKtorServersComponent>("servers")


public class RaptorKtorComponent internal constructor() : RaptorComponent.Base<RaptorKtorComponent>() {

	internal fun complete(context: RaptorContext) =
		DefaultRaptorKtor(
			configuration = KtorConfiguration(
				servers = componentRegistry.oneOrNull(serversComponentKey)?.complete().orEmpty(),
			),
			context = context,
		)


	@RaptorDsl
	public val servers: RaptorKtorServersComponent
		get() = componentRegistry.oneOrRegister(serversComponentKey, ::RaptorKtorServersComponent)
}


@RaptorDsl
public val RaptorAssemblyQuery<RaptorKtorComponent>.servers: RaptorAssemblyQuery<RaptorKtorServersComponent>
	get() = map { it.servers }
