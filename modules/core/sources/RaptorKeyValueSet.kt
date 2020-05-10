package io.fluidsonic.raptor


interface RaptorKeyValueSet {

	operator fun <Value : Any> get(key: RaptorKey<out Value>): Value?
	fun isEmpty(): Boolean
	override fun toString(): String


	companion object
}
