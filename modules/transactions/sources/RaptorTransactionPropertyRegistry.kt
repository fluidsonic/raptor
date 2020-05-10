package io.fluidsonic.raptor


interface RaptorTransactionPropertyRegistry {

	fun <Value : Any> register(key: RaptorTransactionPropertyKey<in Value>, value: Value)
	fun toSet(): RaptorTransactionPropertySet
	override fun toString(): String


	companion object {

		fun default(parentProperties: RaptorPropertySet): RaptorTransactionPropertyRegistry =
			DefaultRaptorTransactionPropertyRegistry(parentProperties = parentProperties)
	}
}
