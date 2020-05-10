package io.fluidsonic.raptor


interface RaptorKeyValueRegistry {

	fun <Value : Any> register(key: RaptorKey<in Value>, value: Value)
	fun toSet(): RaptorKeyValueSet
	override fun toString(): String


	companion object {

		fun default(elementName: String): RaptorKeyValueRegistry =
			DefaultRaptorKeyValueRegistry(elementName = elementName)
	}
}
