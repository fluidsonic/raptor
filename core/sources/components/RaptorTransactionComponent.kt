package io.fluidsonic.raptor

import org.kodein.di.*


@Raptor.Dsl3
class RaptorTransactionComponent internal constructor() : RaptorComponent.KodeinBoundary {

	private val kodeinConfigs: MutableList<Kodein.Builder.() -> Unit> = mutableListOf()


	internal fun complete() = RaptorTransactionConfig(
		kodeinModule = Kodein.Module("raptor/transaction") { // FIXME scoped name
			for (config in kodeinConfigs)
				config()
		}
	)


	override fun kodein(configure: Kodein.Builder.() -> Unit) {
		kodeinConfigs += configure
	}
}
