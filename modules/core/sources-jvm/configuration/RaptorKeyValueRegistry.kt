package io.fluidsonic.raptor


public interface RaptorKeyValueRegistry {

	public fun <Value : Any> register(key: RaptorKey<in Value>, value: Value)
	public fun toSet(): RaptorKeyValueSet
	override fun toString(): String


	public companion object {

		public fun default(elementName: String): RaptorKeyValueRegistry =
			DefaultRaptorKeyValueRegistry(elementName = elementName)
	}
}
