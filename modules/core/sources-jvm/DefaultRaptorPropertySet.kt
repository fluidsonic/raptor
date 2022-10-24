package io.fluidsonic.raptor


internal class DefaultRaptorPropertySet(
	private val delegate: RaptorKeyValueSet,
) : RaptorPropertySet {

	override operator fun <Value : Any> get(key: RaptorPropertyKey<out Value>): Value? =
		delegate[key]


	override fun isEmpty() =
		delegate.isEmpty()


	override fun toString() =
		delegate.toString()
}
