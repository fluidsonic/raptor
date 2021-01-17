package io.fluidsonic.raptor


internal class DefaultRaptorTransactionFactory(
	private val configurations: List<RaptorTransactionConfigurationScope.() -> Unit>,
) : RaptorTransactionFactory {

	override fun createTransaction(context: RaptorContext, configuration: RaptorTransactionConfigurationScope.() -> Unit): DefaultRaptorTransaction =
		DefaultRaptorTransactionBuilder(parentContext = context)
			.apply {
				for (staticConfiguration in configurations)
					staticConfiguration()

				configuration()
			}
			.build()


	object PropertyKey : RaptorPropertyKey<DefaultRaptorTransactionFactory> {

		override fun toString() = "transaction factory"
	}
}
