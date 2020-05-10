package io.fluidsonic.raptor


interface RaptorTransactionContext : RaptorContext, RaptorTransactionScope {

	operator fun <Value : Any> get(key: RaptorTransactionPropertyKey<Value>): Value?
	override fun toString(): String


	companion object
}
