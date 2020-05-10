package io.fluidsonic.raptor


internal class DefaultRaptorPropertySet(
	private val delegate: RaptorKeyValueSet,
	private val parent: RaptorPropertySet? = null
) : RaptorPropertySet {

	@Suppress("UNCHECKED_CAST")
	override operator fun <Value : Any> get(key: RaptorPropertyKey<out Value>): Value? =
		delegate[key] ?: parent?.get(key)


	override fun isEmpty() =
		delegate.isEmpty() && (parent?.isEmpty() ?: true)


	override fun toString() =
		delegate.toString()
}
