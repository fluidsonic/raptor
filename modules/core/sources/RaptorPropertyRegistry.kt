package io.fluidsonic.raptor


interface RaptorPropertyRegistry {

	fun <Value : Any> register(key: RaptorPropertyKey<Value>, value: Value)

	override fun toString(): String


	companion object
}
