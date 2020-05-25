package io.fluidsonic.raptor


internal class DefaultRaptorTransactionBuilder(
	override val context: RaptorContext
) : RaptorTransactionConfigurationScope {

	override val propertyRegistry = RaptorPropertyRegistry.default()


	fun build() =
		DefaultRaptorTransaction(
			context = DefaultRaptorTransactionContext(
				parent = context,
				properties = propertyRegistry.toSet().withFallback(context.properties)
			)
		)
}
