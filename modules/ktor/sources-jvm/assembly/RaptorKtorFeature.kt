package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*


private val ktorComponentKey = RaptorComponentKey<RaptorKtorComponent>("ktor")
private val ktorPropertyKey = RaptorPropertyKey<DefaultRaptorKtor>("ktor")


public object RaptorKtorFeature : RaptorFeature {

	override fun RaptorFeatureConfigurationApplicationScope.applyConfiguration() {
		propertyRegistry.register(ktorPropertyKey, componentRegistry.one(ktorComponentKey).complete(context = lazyContext))
	}


	override fun RaptorFeatureScope.installed() {
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
	get() = ktorInternal ?: throw RaptorFeatureNotInstalledException(RaptorKtorFeature)


internal val RaptorContext.ktorInternal: DefaultRaptorKtor?
	get() = properties[ktorPropertyKey]


@RaptorDsl
public val RaptorTopLevelConfigurationScope.ktor: RaptorKtorComponent
	get() = componentRegistry.oneOrNull(ktorComponentKey) ?: throw RaptorFeatureNotInstalledException(RaptorKtorFeature)
