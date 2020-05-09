package io.fluidsonic.raptor


interface Raptor {

	operator fun <Value : Any> get(key: RaptorKey<Value>): Value?


	companion object
}


@RaptorDsl
fun raptor(configure: RaptorCoreComponent.() -> Unit): Raptor =
	DefaultRaptorCoreComponent().apply(configure).finalize()
