package io.fluidsonic.raptor


interface RaptorTransactionPropertySet : RaptorPropertySet {

	operator fun <Value : Any> get(key: RaptorTransactionPropertyKey<out Value>): Value?


	companion object
}
