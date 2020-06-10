package io.fluidsonic.raptor

import org.kodein.di.*
import org.kodein.di.erased.*


internal class DefaultRaptorKodeinFactory(
	private val module: Kodein.Module
) : RaptorKodeinFactory {

	override fun createKodein(context: RaptorContext, configuration: RaptorKodeinBuilder.() -> Unit): Kodein {
		val module = module
		val parentContext = context.parent
		val parentKodein =
			if (context is RaptorContext.Lazy) parentContext?.kodein
			else context.kodein
		val parentDKodein = parentKodein?.direct

		return Kodein {
			if (parentKodein != null)
				extend(parentKodein)

			import(module, allowOverride = true) // FIXME support proper testing

			bind<RaptorContext>(overrides = parentDKodein?.instanceOrNull<RaptorContext>() != null) with instance(context)

			if (context is RaptorTransactionContext)
				bind<RaptorTransactionContext>(overrides = parentDKodein?.instanceOrNull<RaptorTransactionContext>() != null) with instance(context)

			configuration()
		}
	}
}
