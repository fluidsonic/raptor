package io.fluidsonic.raptor

import org.kodein.di.*


internal class TransactionKodeinRaptorComponent : RaptorComponent.Default<TransactionKodeinRaptorComponent>() {

	val configurations: MutableList<RaptorKodeinBuilder.() -> Unit> = mutableListOf()
	val factoryPropertyKey: RaptorPropertyKey<DefaultRaptorKodeinFactory> = FactoryPropertyKey()


	override fun toString() = "kodein configuration"


	override fun RaptorComponentConfigurationEndScope.onConfigurationEnded() {
		val configurations = configurations.toList()

		propertyRegistry.register(factoryPropertyKey, DefaultRaptorKodeinFactory(Kodein.Module("transaction") { // FIXME diff. name for other components
			for (configuration in configurations)
				configuration()
		}))
	}


	object Key : RaptorComponentKey<TransactionKodeinRaptorComponent> {

		override fun toString() = "kodein"
	}


	private class FactoryPropertyKey : RaptorPropertyKey<DefaultRaptorKodeinFactory> {

		override fun toString() = "kodein factory (transaction)"
	}
}


@RaptorDsl
fun RaptorComponentSet<RaptorTransactionComponent>.kodein(configuration: RaptorKodeinBuilder.() -> Unit) = configure {
	val kodeinComponent = componentRegistry.oneOrNull(TransactionKodeinRaptorComponent.Key) ?: run {
		TransactionKodeinRaptorComponent().also { kodeinComponent ->
			componentRegistry.register(TransactionKodeinRaptorComponent.Key, kodeinComponent)

			val factoryPropertyKey = kodeinComponent.factoryPropertyKey

			onCreate {
				if (parentContext.parent != null) // FIXME includeNested
					return@onCreate

				val factory = parentContext[factoryPropertyKey]
					?: error("Cannot find factory.")

				val kodein = factory.createKodein(context = lazyContext)

				propertyRegistry.register(DKodeinRaptorPropertyKey, kodein.direct)
				propertyRegistry.register(KodeinRaptorPropertyKey, kodein)
			}
		}
	}

	kodeinComponent.configurations += configuration
}
