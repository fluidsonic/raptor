package io.fluidsonic.raptor


interface RaptorTransaction {

	val isComplete: Boolean

	// FIXME remove and only allow runWithNewTransaction for now
	fun complete()
}
