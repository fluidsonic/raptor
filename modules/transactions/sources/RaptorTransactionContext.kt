package io.fluidsonic.raptor


interface RaptorTransactionContext : RaptorContext {

	override val properties: RaptorTransactionPropertySet

	override fun asScope(): RaptorTransactionScope
	override fun toString(): String


	companion object
}


operator fun <Value : Any> RaptorTransactionContext.get(key: RaptorTransactionPropertyKey<out Value>) =
	properties[key]
