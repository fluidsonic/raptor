package io.fluidsonic.raptor


interface Raptor {

	operator fun <Value : Any> get(key: RaptorPropertyKey<Value>): Value?
	override fun toString(): String


	companion object
}


@RaptorDsl
fun raptor(configure: RaptorCoreComponent.() -> Unit): Raptor =
	DefaultRaptorCoreComponent().apply(configure).finalize()
