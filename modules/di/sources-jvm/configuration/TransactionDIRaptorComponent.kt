package io.fluidsonic.raptor


internal class TransactionDIRaptorComponent : RaptorComponent.Default<TransactionDIRaptorComponent>() {

	val builder = DefaultRaptorDIBuilder()
	val factoryPropertyKey: RaptorPropertyKey<RaptorDI.Factory> = FactoryPropertyKey()


	override fun toString() = "transaction DI configuration"


	override fun RaptorComponentConfigurationEndScope.onConfigurationEnded() {
		// FIXME Use different names for different components. Can we take the component hierarchy into account?
		propertyRegistry.register(factoryPropertyKey, DefaultRaptorDI.Factory(modules = listOf(builder.createModule(name = "transaction"))))
	}


	object Key : RaptorComponentKey<TransactionDIRaptorComponent> {

		override fun toString() = "transaction DI"
	}


	private class FactoryPropertyKey : RaptorPropertyKey<RaptorDI.Factory> {

		override fun toString() = "transaction DI factory"
	}
}


@RaptorDsl
public fun RaptorComponentSet<RaptorTransactionComponent>.di(configuration: RaptorDIBuilder.() -> Unit) {
	configure {
		val diComponent = componentRegistry.oneOrNull(TransactionDIRaptorComponent.Key) ?: run {
			TransactionDIRaptorComponent().also { diComponent ->
				componentRegistry.register(TransactionDIRaptorComponent.Key, diComponent)

				val factoryPropertyKey = diComponent.factoryPropertyKey

				onCreate {
					// TODO Do we want to support nested transactions here?. raptor-ktor uses them (root tx -> route tx -> route tx -> …)
//					if (parentContext.parent != null)
//						return@onCreate

					val factory = parentContext[factoryPropertyKey]
						?: error("Cannot find dependency injection factory.")

					propertyRegistry.register(DIRaptorPropertyKey, factory.createDI(context = lazyContext))
				}
			}
		}

		diComponent.builder.apply(configuration)
	}
}
