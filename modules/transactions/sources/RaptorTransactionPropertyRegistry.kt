package io.fluidsonic.raptor


interface RaptorTransactionPropertyRegistry {

	fun <Value : Any> register(key: RaptorTransactionPropertyKey<Value>, value: Value)

	override fun toString(): String


	companion object
}
