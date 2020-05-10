package io.fluidsonic.raptor


internal class DefaultRaptorTransactionFactory(
	private val configurations: List<RaptorTransactionCreationScope.() -> Unit>
) {

	fun createTransaction(context: RaptorContext) =
		DefaultRaptorTransactionBuilder(context = context)
			.apply {
				for (action in configurations)
					action()
			}
			.build()


	object PropertyKey : RaptorPropertyKey<DefaultRaptorTransactionFactory> {

		override fun toString() = "transaction factory"
	}
}
