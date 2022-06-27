package io.fluidsonic.raptor

import kotlin.reflect.*


internal class TransactionDIRaptorComponent : RaptorComponent.Default<RaptorDIComponent>(), RaptorDIComponent {

	private val builder = DefaultRaptorDIBuilder()
	internal val factoryPropertyKey: RaptorPropertyKey<RaptorDI.Factory> = FactoryPropertyKey()


	override fun provide(type: KType, provide: RaptorDI.() -> Any?) {
		builder.provide(type = type, provide = provide)
	}


	override fun toString() =
		"transaction DI configuration"


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
public val RaptorComponentSet<RaptorTransactionComponent>.di: RaptorComponentSet<RaptorDIComponent>
	get() = withComponentAuthoring {
		map {
			componentRegistry.oneOrRegister(TransactionDIRaptorComponent.Key) {
				TransactionDIRaptorComponent().also { diComponent ->
					val factoryPropertyKey = diComponent.factoryPropertyKey

					onCreate {
						val factory = parentContext[factoryPropertyKey]
							?: error("Cannot find dependency injection factory.")

						propertyRegistry.register(DIRaptorPropertyKey, factory.createDI<RaptorTransactionContext>(context = lazyContext))
					}
				}
			}
		}
	}
