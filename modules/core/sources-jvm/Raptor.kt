package io.fluidsonic.raptor


interface Raptor {

	val context: RaptorContext

	override fun toString(): String


	companion object
}


operator fun <Value : Any> Raptor.get(key: RaptorPropertyKey<out Value>): Value? =
	context[key]


val Raptor.properties
	get() = context.properties


@RaptorDsl
fun raptor(configure: RaptorRootComponent.() -> Unit): Raptor =
	DefaultRaptorRootComponent().apply(configure).endConfiguration()
