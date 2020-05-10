package io.fluidsonic.raptor

import org.kodein.di.*


object RaptorKodeinFeature : RaptorFeature {

	override fun RaptorFeatureFinalizationScope.finalize() {
		val kodeinModule = componentRegistry.one(DefaultRaptorKodeinComponent.Key).finalize()

		propertyRegistry.register(
			DefaultRaptorKodeinPropertyKey,
			Kodein {
				import(kodeinModule, allowOverride = true) // FIXME add special facility for testing
			}
		)
	}


	override fun RaptorFeatureInstallationScope.install() {
		componentRegistry.register(DefaultRaptorKodeinComponent.Key, DefaultRaptorKodeinComponent())

		transactions {
			onCreate {
				if (context[DefaultRaptorKodeinTransactionPropertyKey] != null)

				val configurations = extensions[DefaultRaptorComponentKodeinExtension.Key]?.configurations?.toList().orEmpty() // FIXME wrong: comp leak,scope
				val kodeinModule = Kodein.Module("raptor/transaction") { // FIXME nesting recursion
					for (configuration in configurations)
						configuration()
				}

				propertyRegistry.register(DefaultRaptorKodeinTransactionPropertyKey, Kodein {
					extend(context.kodein)

					import(kodeinModule, allowOverride = true) // FIXME support proper testing
				})
			}
		}
	}
}


val Raptor.kodein: Kodein
	get() = context.kodein


val RaptorContext.kodein: Kodein
	get() = properties[DefaultRaptorKodeinPropertyKey]
		?: error("You must install ${RaptorKodeinFeature::class.simpleName} in order to use Kodein.")


val RaptorTransaction.kodein: Kodein
	get() = context.kodein


val RaptorTransactionContext.kodein: Kodein
	get() = properties[DefaultRaptorKodeinTransactionPropertyKey]
		?: error("You must install ${RaptorKodeinFeature::class.simpleName} in order to use Kodein.")


@RaptorDsl
fun RaptorGlobalConfigurationScope.kodein(configuration: RaptorKodeinBuilder.() -> Unit) {
	componentRegistry.configure(DefaultRaptorKodeinComponent.Key) {
		configurations += configuration
	}
}


@RaptorDsl
fun RaptorComponentSet<RaptorTransactionComponent>.kodein(configuration: RaptorKodeinBuilder.() -> Unit) = configure {
	extensions.getOrSet(DefaultRaptorComponentKodeinExtension.Key) { DefaultRaptorComponentKodeinExtension() }
		.configurations
		.add(configuration)
}
