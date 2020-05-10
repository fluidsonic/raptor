package io.fluidsonic.raptor


internal class DefaultRaptorTransactionPropertyRegistry(
	private val parentProperties: RaptorPropertySet
) : RaptorTransactionPropertyRegistry {

	private val delegate = RaptorKeyValueRegistry.default(elementName = "property")


	override fun <Value : Any> register(key: RaptorTransactionPropertyKey<in Value>, value: Value) =
		delegate.register(key, value)


	override fun toSet() =
		DefaultRaptorTransactionPropertySet(
			delegate = delegate.toSet(),
			parent = parentProperties
		)


	override fun toString() =
		delegate.toString()
}
