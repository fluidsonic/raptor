package io.fluidsonic.raptor


internal class DefaultRaptorTransactionFactory(
	private val configurations: List<RaptorTransactionConfigurationScope.() -> Unit>
) : RaptorTransactionFactory {

	override fun createTransaction(context: RaptorContext, configuration: RaptorTransactionConfigurationScope.() -> Unit) =
		DefaultRaptorTransactionBuilder(context = context)
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
