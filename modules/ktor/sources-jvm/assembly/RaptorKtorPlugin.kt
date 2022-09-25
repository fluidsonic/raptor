package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*


private val ktorComponentKey = RaptorComponentKey<RaptorKtorComponent>("ktor")
private val ktorPropertyKey = RaptorPropertyKey<DefaultRaptorKtor>("ktor")


public object RaptorKtorPlugin : RaptorPlugin {

	override fun RaptorPluginCompletionScope.complete() {
		propertyRegistry.register(ktorPropertyKey, componentRegistry.one(ktorComponentKey).complete(context = lazyContext))
	}


	override fun RaptorPluginInstallationScope.install() {
		componentRegistry.register(ktorComponentKey, ::RaptorKtorComponent)

		lifecycle {
			onStart {
				checkNotNull(context.ktorInternal).start()
			}
			onStop {
				checkNotNull(context.ktorInternal).stop()
			}
		}
	}


	override fun toString(): String = "ktor"
}


public val RaptorContext.ktor: RaptorKtor
	get() = ktorInternal ?: throw RaptorPluginNotInstalledException(RaptorKtorPlugin)


internal val RaptorContext.ktorInternal: DefaultRaptorKtor?
	get() = properties[ktorPropertyKey]


@RaptorDsl
public val RaptorAssemblyScope.ktor: RaptorKtorComponent
	get() = componentRegistry.oneOrNull(ktorComponentKey) ?: throw RaptorPluginNotInstalledException(RaptorKtorPlugin)
