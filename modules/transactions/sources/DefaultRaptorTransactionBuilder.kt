package io.fluidsonic.raptor


internal class DefaultRaptorTransactionBuilder(
	override val context: RaptorContext
) : RaptorTransactionCreationScope {

	override val propertyRegistry = RaptorTransactionPropertyRegistry.default(parentProperties = context.properties)


	fun build() =
		DefaultRaptorTransaction(
			context = DefaultRaptorTransactionContext(
				parent = context,
				properties = propertyRegistry.toSet()
			)
		)
}
