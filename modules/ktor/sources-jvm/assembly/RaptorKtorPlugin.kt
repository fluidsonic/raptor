package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*


public object RaptorKtorPlugin : RaptorPlugin {

	override fun RaptorPluginCompletionScope.complete() {
		completeComponents()

		propertyRegistry.register(Keys.ktorProperty, componentRegistry.one(Keys.ktorComponent).complete(context = lazyContext))
	}


	override fun RaptorPluginInstallationScope.install() {
		require(RaptorLifecyclePlugin)
		require(RaptorTransactionPlugin)

		componentRegistry.register(Keys.ktorComponent, RaptorKtorComponent())

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


@RaptorDsl
public val RaptorAssemblyScope.ktor: RaptorKtorComponent
	get() = componentRegistry.oneOrNull(Keys.ktorComponent) ?: throw RaptorPluginNotInstalledException(RaptorKtorPlugin)
