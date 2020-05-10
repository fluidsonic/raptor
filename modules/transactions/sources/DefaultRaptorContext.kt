package io.fluidsonic.raptor


internal class DefaultRaptorContext : RaptorContext {

	override val context
		get() = this


	override fun createTransaction() =
		DefaultRaptorTransaction(parentContext = this)


	override fun toString() = "global context"


	object PropertyKey : RaptorPropertyKey<DefaultRaptorContext> {

		override fun toString() = "context"
	}
}
