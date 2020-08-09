package io.fluidsonic.raptor

import kotlin.reflect.full.*


internal class DefaultRaptorDIFactory(
	private val modules: List<DefaultRaptorDIModule>,
) : RaptorDIFactory {

	override fun createDI(context: RaptorContext, configuration: RaptorDIBuilder.() -> Unit): RaptorDI {
		val parentContext = context.parent
		val parentDI = when (context) {
			is RaptorContext.Lazy -> parentContext?.di
			else -> context.di
		}

		val contextModule = DefaultRaptorDIModule(
			name = "raptor/context",
			provideByType = mapOf(context::class.starProjectedType to {
				(context as? RaptorContext.Lazy)?.context ?: context
			})
		)

		val inlineModule = DefaultRaptorDIBuilder()
			.apply(configuration)
			.createModule(name = "inline")
			.takeIf { it.provideByType.isNotEmpty() }

		return DefaultRaptorDI(
			modules = modules + listOfNotNull(inlineModule, contextModule),
			parent = parentDI
		)
	}
}
