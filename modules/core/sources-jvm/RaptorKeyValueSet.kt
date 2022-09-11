package io.fluidsonic.raptor


public interface RaptorKeyValueSet {

	public operator fun <Value : Any> get(key: RaptorKey<out Value>): Value?
	public fun isEmpty(): Boolean
	override fun toString(): String


	public companion object
}
