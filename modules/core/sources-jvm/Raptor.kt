package io.fluidsonic.raptor


public interface Raptor {

	public val context: RaptorContext

	override fun toString(): String


	public companion object
}


public operator fun <Value : Any> Raptor.get(key: RaptorPropertyKey<out Value>): Value? =
	context[key]


public val Raptor.properties: RaptorPropertySet
	get() = context.properties
