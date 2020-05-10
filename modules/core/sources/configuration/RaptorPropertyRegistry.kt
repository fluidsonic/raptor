package io.fluidsonic.raptor


interface RaptorPropertyRegistry {

	fun <Value : Any> register(key: RaptorPropertyKey<in Value>, value: Value)
	fun toSet(): RaptorPropertySet
	override fun toString(): String


	companion object {

		fun default(parent: RaptorPropertySet? = null): RaptorPropertyRegistry =
			DefaultRaptorPropertyRegistry(parent = parent)
	}
}
