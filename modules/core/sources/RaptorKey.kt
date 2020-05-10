package io.fluidsonic.raptor


// FIXME Make toString() (or name) mandatory?
interface RaptorKey<Value : Any> {

	override fun toString(): String


	companion object
}
