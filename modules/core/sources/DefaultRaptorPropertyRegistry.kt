package io.fluidsonic.raptor


internal class DefaultRaptorPropertyRegistry(
	private val parentProperties: RaptorPropertySet? = null
) : RaptorPropertyRegistry {

	private val delegate = RaptorKeyValueRegistry.default(elementName = "property")


	override fun <Value : Any> register(key: RaptorPropertyKey<in Value>, value: Value) =
		delegate.register(key, value)


	override fun toSet() =
		DefaultRaptorPropertySet(
			delegate = delegate.toSet(),
			parent = parentProperties
		)


	override fun toString() =
		delegate.toString()
}
