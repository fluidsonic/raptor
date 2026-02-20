package io.fluidsonic.raptor.service2

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*


internal interface TestService : RaptorService2


internal class TestServiceImpl : TestService


internal object TestRaptorContext : RaptorContext {

	override val parent: RaptorContext? = null
	override val properties: RaptorPropertySet = RaptorPropertySet.empty()

	override fun toString(): String = "TestRaptorContext"
}


internal class TestDI(private val providers: Map<RaptorDIKey<*>, () -> Any?>) : RaptorDI {

	@Suppress("UNCHECKED_CAST")
	override fun <Value> get(key: RaptorDIKey<out Value>): Value {
		val provider = providers[key] ?: error("No provider for key: $key")
		return provider() as Value
	}


	@Suppress("UNCHECKED_CAST")
	override fun <Value : Any> getOrNull(key: RaptorDIKey<out Value?>): Value? {
		val provider = providers[key] ?: return null
		return provider() as Value?
	}


	override fun <Value> invoke(factory: RaptorDI.() -> Value): Lazy<Value> =
		lazy { factory() }


	override fun toString(): String = "TestDI"
}
