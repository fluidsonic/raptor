package io.fluidsonic.raptor


class RaptorTransactionConfigurationComponent internal constructor() : RaptorComponent {

	private val configurations = mutableListOf<RaptorTransactionSetup.() -> Unit>()


	internal fun add(configuration: RaptorTransactionSetup.() -> Unit) {
		configurations += configuration
	}
}
