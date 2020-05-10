package io.fluidsonic.raptor


internal class DefaultRaptorTransactionBuilder(
	override val context: RaptorContext
) : RaptorTransactionConfigurationScope {

	override val propertyRegistry = RaptorPropertyRegistry.default(parentProperties = context.properties)


	fun build() =
		DefaultRaptorTransaction(
			context = DefaultRaptorTransactionContext(
				parent = context,
				properties = propertyRegistry.toSet()
			)
		)
}
