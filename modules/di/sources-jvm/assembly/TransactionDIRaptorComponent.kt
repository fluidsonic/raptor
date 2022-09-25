package io.fluidsonic.raptor.di

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*
import kotlin.reflect.*


private val key = RaptorComponentKey<TransactionDIRaptorComponent>("transaction DI")


private class TransactionDIRaptorComponent :
	RaptorComponent.Base<TransactionDIRaptorComponent>(RaptorDIPlugin),
	RaptorDIComponent<TransactionDIRaptorComponent> {

	private val builder = DefaultRaptorDIBuilder()

	// TODO Use different names for different components. Can we take the component hierarchy into account?
	val factoryPropertyKey: RaptorPropertyKey<RaptorDI.Factory> = RaptorPropertyKey("transaction DI factory")


	override fun provide(type: KType, provide: RaptorDI.() -> Any?) {
		builder.provide(type = type, provide = provide)
	}


	override fun toString() =
		"transaction DI configuration"


	override fun RaptorComponentConfigurationEndScope<TransactionDIRaptorComponent>.onConfigurationEnded() {
		// TODO Use different names for different components. Can we take the component hierarchy into account?
		propertyRegistry.register(factoryPropertyKey, DefaultRaptorDI.Factory(modules = listOf(builder.createModule(name = "transaction"))))
	}
}


@RaptorDsl
public val RaptorTransactionsComponent.di: RaptorDIComponent<*>
	get() = componentRegistry.oneOrRegister(key) {
		TransactionDIRaptorComponent().also { diComponent ->
			val factoryPropertyKey = diComponent.factoryPropertyKey

			onCreate {
				val factory = parentContext[factoryPropertyKey]
					?: error("Cannot find dependency injection factory.")

				propertyRegistry.register(factory.createDI<RaptorTransactionContext>(context = lazyContext))
			}
		}
	}


@RaptorDsl
public val RaptorAssemblyQuery<RaptorTransactionsComponent>.di: RaptorAssemblyQuery<RaptorDIComponent<*>>
	get() = map { it.di }
