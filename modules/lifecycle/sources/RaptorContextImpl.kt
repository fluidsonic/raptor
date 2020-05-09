package io.fluidsonic.raptor

import org.kodein.di.*
import org.kodein.di.erased.*


internal class RaptorContextImpl(
	kodeinModule: Kodein.Module
) : RaptorContext {

	override val dkodein = Kodein.direct {
		import(kodeinModule, allowOverride = true) // FIXME add special facility for testing

		bind<RaptorContext>() with instance(this@RaptorContextImpl)
		bind<RaptorScope>() with instance(this@RaptorContextImpl)
	}


	override val context
		get() = this


	override fun createTransaction() =
		RaptorTransactionImpl(parentContext = this)


	override fun createTransactionKodein(config: Kodein.Builder.() -> Unit) = Kodein.direct(allowSilentOverride = true) {
		extend(dkodein)

		for (contextConfig in dkodein.allInstances<RaptorTransactionConfig>())
			import(contextConfig.kodeinModule)

		config()
	}
}
