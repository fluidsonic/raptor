package io.fluidsonic.raptor

import org.kodein.di.*


object RaptorKodeinFeature : RaptorFeature {

	override fun RaptorFeatureFinalizationScope.finalize() {
		componentRegistry.one(DefaultRootKodeinRaptorComponent.Key)
			.finalize(propertyRegistry = propertyRegistry)
	}


	override fun RaptorFeatureInstallationScope.install() {
		componentRegistry.register(DefaultRootKodeinRaptorComponent.Key, DefaultRootKodeinRaptorComponent())
	}
}


val Raptor.kodein: Kodein
	get() = context.kodein


val RaptorContext.kodein: Kodein
	get() = properties[DefaultKodeinRaptorPropertyKey]
		?: error("You must install ${RaptorKodeinFeature::class.simpleName} to use Kodein.")


val RaptorTransaction.kodein: Kodein
	get() = context.kodein


@RaptorDsl
fun RaptorGlobalConfigurationScope.kodein(configuration: RaptorKodeinBuilder.() -> Unit) {
	componentRegistry.configure(DefaultRootKodeinRaptorComponent.Key) {
		configurations += configuration
	}
}


@RaptorDsl
fun RaptorComponentSet<RaptorTransactionComponent>.kodein(configuration: RaptorKodeinBuilder.() -> Unit) = configure {
	val kodeinComponent = componentRegistry.oneOrNull(DefaultTransactionKodeinRaptorComponent.Key) ?: run {
		DefaultTransactionKodeinRaptorComponent().also { kodeinComponent ->
			val factoryPropertyKey = kodeinComponent.factoryPropertyKey

			onCreate {
				if (context.parent != null) // FIXME
					return@onCreate

				val factory = context[factoryPropertyKey]
					?: error("Cannot find factory.")

				propertyRegistry.register(DefaultKodeinRaptorPropertyKey, factory.createKodein(context = context))
			}

			componentRegistry.root.configure(DefaultRootKodeinRaptorComponent.Key) {
				scopedComponents += kodeinComponent
			}
		}
	}

	kodeinComponent.configurations += configuration
}
