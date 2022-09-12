package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*


public object RaptorKtorFeature : RaptorFeature {

	override fun RaptorFeatureScope.installed() {
		componentRegistry2.register(RaptorKtorComponent.Key, ::RaptorKtorComponent)

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
public val RaptorTopLevelConfigurationScope.ktor: RaptorKtorComponent
	get() = componentRegistry2.oneOrNull(RaptorKtorComponent.Key) ?: throw RaptorFeatureNotInstalledException(RaptorKtorFeature)
