package io.fluidsonic.raptor


internal class DefaultPropertyRegistry : RaptorPropertyRegistry {

	private val delegate = RaptorKeyValueRegistry.default(elementName = "property")


	override fun <Value : Any> register(key: RaptorPropertyKey<in Value>, value: Value) =
		delegate.register(key, value)


	override fun toSet() =
		delegate.toSet().let { keyValueSet ->
			if (keyValueSet.isEmpty()) RaptorPropertySet.empty()
			else DefaultRaptorPropertySet(delegate = delegate.toSet())
		}


	override fun toString() =
		delegate.toString()
}
