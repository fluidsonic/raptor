package io.fluidsonic.raptor.di

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*
import kotlin.reflect.*


internal class TransactionDIRaptorComponent : RaptorComponent2.Base(), RaptorDIComponent {

	private val builder = DefaultRaptorDIBuilder()

	internal val factoryPropertyKey: RaptorPropertyKey<RaptorDI.Factory> = FactoryPropertyKey()


	override fun provide(type: KType, provide: RaptorDI.() -> Any?) {
		builder.provide(type = type, provide = provide)
	}


	override fun toString() =
		"transaction DI configuration"


	override fun RaptorComponentConfigurationEndScope2.onConfigurationEnded() {
		// FIXME Use different names for different components. Can we take the component hierarchy into account?
		propertyRegistry.register(factoryPropertyKey, DefaultRaptorDI.Factory(modules = listOf(builder.createModule(name = "transaction"))))
	}


	object Key : RaptorComponentKey2<TransactionDIRaptorComponent> {

		override fun toString() = "transaction DI"
	}


	private class FactoryPropertyKey : RaptorPropertyKey<RaptorDI.Factory> {

		override fun toString() = "transaction DI factory"
	}
}


@RaptorDsl
public val RaptorTransactionComponent.di: RaptorDIComponent
	get() = componentRegistry2.oneOrRegister(TransactionDIRaptorComponent.Key) {
		TransactionDIRaptorComponent().also { diComponent ->
			val factoryPropertyKey = diComponent.factoryPropertyKey

			onCreate {
				val factory = parentContext[factoryPropertyKey]
					?: error("Cannot find dependency injection factory.")

				propertyRegistry.register(DIRaptorPropertyKey, factory.createDI<RaptorTransactionContext>(context = lazyContext))
			}
		}
	}


@RaptorDsl
public val RaptorAssemblyQuery2<RaptorTransactionComponent>.di: RaptorAssemblyQuery2<RaptorDIComponent>
	get() = map { it.di }
