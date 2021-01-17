package io.fluidsonic.raptor

import io.fluidsonic.raptor.di.*
import java.util.concurrent.locks.*
import kotlin.concurrent.*
import kotlin.reflect.*
import kotlin.reflect.full.*


internal class DefaultRaptorDI(
	modules: List<RaptorDI.Module>,
	private val parent: RaptorDI?,
) : RaptorDI {

	private val dependenciesByModule: Map<RaptorDI.Module, MutableMap<KType, Any?>> = modules
		.reversed()
		.associateWith { hashMapOf() }

	private val dependenciesByType: MutableMap<KType, Any?> = hashMapOf()
	private val currentlyResolvingTypes = mutableListOf<KType>()
	private val lock = ReentrantLock()


	@Suppress("NAME_SHADOWING")
	override fun get(type: KType): Any? {
		val isNullable = type.isMarkedNullable
		val type = type.withNullability(false)

		val value = getOrResolveDependency(type)
		if (value == null && !isNullable)
			reportMissingDependency(type)

		return value
	}


	private fun getOrResolveDependency(type: KType): Any? =
		lock.withLock { // TODO Add fast-path if dependency is already resolved.
			dependenciesByType.getOrPutNullable(type) {
				if (currentlyResolvingTypes.contains(type))
					reportCyclicDependency(type)

				currentlyResolvingTypes += type

				try {
					resolve(type)
				}
				finally {
					currentlyResolvingTypes.removeLast()
				}
			}
		}


	override fun <Value> invoke(factory: RaptorDI.() -> Value) =
		lazy { factory() }


	private fun reportCyclicDependency(type: KType): Nothing {
		error(buildString {
			append("Dependency injection has encountered a dependency cycle:\n\n")

			currentlyResolvingTypes.forEachIndexed { index, resolvingType ->
				repeat(index) { append('\t') }
				append(resolvingType)

				if (resolvingType == type)
					append("  <--")

				append('\n')
			}

			repeat(currentlyResolvingTypes.size) { append('\t') }
			append(type)
			append("  <--\n\nDI:\n")
			append(this) // FIXME will print wrong DI - `this` may not be the DI that initiated the resolution
		})
	}


	private fun reportMissingDependency(type: KType): Nothing {
		error("Cannot resolve dependency of type '$type'.\n\nDI:\n$this") // FIXME will print wrong DI - `this` may not be the DI that initiated the resolution
	}


	private fun resolve(type: KType): Any? {
		dependenciesByModule.forEach { (module, moduleDependenciesByType) ->
			val provider = module.providerForType(type) ?: return@forEach

			val value = when (provider.type) {
				type -> provider.provide(this@DefaultRaptorDI) ?: return@forEach
				else -> getOrResolveDependency(provider.type)
			}

			moduleDependenciesByType[type] = value

			return value
		}

		parent?.let { parent ->
			return (parent as DefaultRaptorDI).getOrResolveDependency(type) // FIXME make abstract
		}

		return null
	}


	override fun toString() = buildString {
		val processedTypes = hashSetOf<KType>()

		dependenciesByModule.forEach { (module, moduleDependenciesByType) ->
			append(module.name)
			append(" {")

			if (module.providers.isNotEmpty()) {
				append("\n")
				module.providers
					.map { provider -> provider to provider.type.toString() }
					.sortedBy { it.second }
					.forEach { (provider, typeString) ->
						val type = provider.type

						append("\t")
						append(typeString)
						append(" = ")

						if (processedTypes.add(type))
							when (moduleDependenciesByType.containsKey(type)) {
								true -> append(moduleDependenciesByType[type]?.let { it::class.qualifiedName ?: "<anonymous class>" })
								false -> append("<not yet requested>")
							}
						else
							append("<overridden>")

						append("\n")
					}
			}

			append("}\n")
		}

		parent?.let { parent ->
			append("\nParent DI:\n")
			append(parent)
		}
	}


	internal class Factory(
		private val modules: List<RaptorDI.Module>,
	) : RaptorDI.Factory {

		override fun createDI(context: RaptorContext, configuration: RaptorDIBuilder.() -> Unit): RaptorDI {
			val parentContext = context.parent
			val parentDI = when (context) {
				is RaptorContext.Lazy -> parentContext?.di
				else -> context.di
			}

			val contextModule = Module(
				name = "raptor (context)",
				providers = listOf(RaptorDI.provider(context::class.starProjectedType) {
					(context as? RaptorContext.Lazy)?.context ?: context
				})
			)

			val inlineModule = DefaultRaptorDIBuilder()
				.apply(configuration)
				.createModule(name = "inline")
				.takeIf { it.providers.isNotEmpty() }

			return DefaultRaptorDI(
				modules = modules + listOfNotNull(inlineModule, contextModule),
				parent = parentDI
			)
		}
	}


	internal class Module(
		override val name: String,
		override val providers: List<RaptorDI.Provider>,
	) : RaptorDI.Module


	internal class Provider(
		private val provide: RaptorDI.() -> Any?,
		override val type: KType,
	) : RaptorDI.Provider {

		override fun provide(di: RaptorDI) =
			with(di) { provide() }
	}
}
