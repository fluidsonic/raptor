package io.fluidsonic.raptor


internal class DefaultRaptorTransactionBuilder(
	override val parentContext: RaptorContext
) : RaptorTransactionConfigurationScope {

	override val lazyContext = LazyRaptorTransactionContext(parent = parentContext)
	override val propertyRegistry = RaptorPropertyRegistry.default()


	fun build(): DefaultRaptorTransaction {
		val context = DefaultRaptorTransactionContext(
			parent = parentContext,
			properties = propertyRegistry.toSet().withFallback(parentContext.properties)
		)

		lazyContext.resolve(context)

		return DefaultRaptorTransaction(context = context)
	}
}
