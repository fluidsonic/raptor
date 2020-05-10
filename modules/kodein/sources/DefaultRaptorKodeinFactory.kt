package io.fluidsonic.raptor

import org.kodein.di.*


internal class DefaultRaptorKodeinFactory(
	private val module: Kodein.Module
) : RaptorKodeinFactory {

	override fun createKodein(context: RaptorContext, configuration: RaptorKodeinBuilder.() -> Unit): Kodein {
		val module = module
		val parentKodein = context.kodein

		return Kodein {
			extend(parentKodein)
			import(module, allowOverride = true) // FIXME support proper testing

			configuration()
		}
	}


	object PropertyKey : RaptorPropertyKey<DefaultRaptorKodeinFactory> {

		override fun toString() = "kodein factory"
	}
}
