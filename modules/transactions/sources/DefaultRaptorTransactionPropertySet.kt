package io.fluidsonic.raptor


internal class DefaultRaptorTransactionPropertySet(
	private val delegate: RaptorKeyValueSet,
	private val parent: RaptorPropertySet
) : RaptorTransactionPropertySet, RaptorPropertySet by parent {

	@Suppress("UNCHECKED_CAST")
	override operator fun <Value : Any> get(key: RaptorTransactionPropertyKey<out Value>): Value? =
		delegate[key] ?: (parent as? RaptorTransactionPropertySet)?.get(key)


	override fun isEmpty() =
		delegate.isEmpty() && parent.isEmpty()


	override fun toString() =
		TODO() // FIXME
}
