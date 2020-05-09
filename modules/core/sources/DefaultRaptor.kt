package io.fluidsonic.raptor


internal class DefaultRaptor(
	val properties: Map<RaptorKey<*>, Any>
) : Raptor {

	@Suppress("UNCHECKED_CAST")
	override fun <Value : Any> get(key: RaptorKey<Value>) =
		properties[key] as Value?
}
