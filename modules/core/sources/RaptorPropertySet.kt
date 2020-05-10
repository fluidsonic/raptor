package io.fluidsonic.raptor


interface RaptorPropertySet {

	operator fun <Value : Any> get(key: RaptorPropertyKey<out Value>): Value?
	fun isEmpty(): Boolean
	override fun toString(): String


	companion object
}
