package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*


public class RaptorKtorComponent internal constructor() : RaptorComponent.Base<RaptorKtorComponent>(RaptorKtorPlugin) {

	internal fun complete(context: RaptorContext) =
		RaptorKtorInternal(
			configuration = KtorConfiguration(
				servers = componentRegistry.oneOrNull(Keys.serversComponent)?.complete().orEmpty(),
			),
			context = context,
		)


	@RaptorDsl
	public val servers: RaptorKtorServersComponent
		get() = componentRegistry.oneOrRegister(Keys.serversComponent, ::RaptorKtorServersComponent)
}


@RaptorDsl
public val RaptorAssemblyQuery<RaptorKtorComponent>.servers: RaptorAssemblyQuery<RaptorKtorServersComponent>
	get() = map { it.servers }
