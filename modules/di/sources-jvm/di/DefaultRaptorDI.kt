package io.fluidsonic.raptor.di

import io.fluidsonic.raptor.*
import java.util.concurrent.locks.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.concurrent.*


internal class DefaultRaptorDI(
	modules: List<RaptorDI.Module>,
	private val parent: RaptorDI?,
) : RaptorDI {

	private val dependenciesByModule: Map<RaptorDI.Module, MutableMap<RaptorDIKey<*>, Any?>> = modules
		.reversed()
		.associateWith { hashMapOf() }

	private val dependenciesByKey: MutableMap<RaptorDIKey<*>, Any?> = hashMapOf()
	private val currentlyResolvingKeys = mutableListOf<RaptorDIKey<*>>()
	private val lock = ReentrantLock()


	override fun <Value> get(key: RaptorDIKey<out Value>): Value {
		val value = getOrNull(key)
		if (value != null)
			return value

		if (key.isOptional)
			@Suppress("UNCHECKED_CAST")
			return null as Value

		reportMissingDependency(key)
	}


	@Suppress("UNCHECKED_CAST")
	override fun <Value : Any> getOrNull(key: RaptorDIKey<out Value?>): Value? =
		lock.withLock { // TODO Add fast-path if dependency is already resolved.
			dependenciesByKey.getOrPutNullable(key) {
				val notOptionalKey = key.notOptional()

				if (currentlyResolvingKeys.contains(notOptionalKey))
					reportCyclicDependency(notOptionalKey)

				currentlyResolvingKeys += notOptionalKey

				try {
					resolve(notOptionalKey)
				}
				finally {
					currentlyResolvingKeys.removeLast()
				}
			} as Value?
		}


	override fun <Value> invoke(factory: RaptorDI.() -> Value) =
		lazy { factory() }


	private fun reportCyclicDependency(key: RaptorDIKey<*>): Nothing {
		error(buildString {
			append("Dependency injection has encountered a dependency cycle:\n\n")

			currentlyResolvingKeys.forEachIndexed { index, resolvingKey ->
				repeat(index) { append('\t') }
				append(resolvingKey)

				if (resolvingKey == key)
					append("  <--")

				append('\n')
			}

			repeat(currentlyResolvingKeys.size) { append('\t') }
			append(key)
			append("  <--\n\nDI:\n")
			append(this) // TODO will print wrong DI - `this` may not be the DI that initiated the resolution
		})
	}


	private fun reportMissingDependency(key: RaptorDIKey<*>): Nothing {
		error("Cannot resolve dependency for key '$key'.\n\nDI:\n$this") // TODO will print wrong DI - `this` may not be the DI that initiated the resolution
	}


	private fun <Value> resolve(key: RaptorDIKey<out Value>): Value? {
		assert(!key.isOptional) { "Key must not be optional at this point." }

		dependenciesByModule.forEach { (module, moduleDependenciesByKey) ->
			val provider = module.providerForKey(key) ?: return@forEach
			val value = provider.provide(this@DefaultRaptorDI) ?: return@forEach
			moduleDependenciesByKey[key] = value

			return value
		}

		parent?.let { parent ->
			return (parent as DefaultRaptorDI).getOrNull(key) // TODO make abstract
		}

		return null
	}


	override fun toString() = buildString {
		val processedKeys = hashSetOf<RaptorDIKey<*>>()

		dependenciesByModule.forEach { (module, moduleDependenciesByKey) ->
			append(module.name)
			append(" {")

			if (module.providers.isNotEmpty()) {
				append("\n")
				module.providers
					.map { provider -> provider to provider.key.toString() }
					.sortedBy { it.second }
					.forEach { (provider, typeString) ->
						val key = provider.key

						append("\t")
						append(typeString)
						append(" = ")

						if (processedKeys.add(key))
							when (moduleDependenciesByKey.containsKey(key)) {
								true -> append(moduleDependenciesByKey[key]?.let { it::class.qualifiedName ?: "<anonymous class>" })
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

		override fun <Context : RaptorContext> createDI(
			context: Context,
			key: RaptorDIKey<in Context>,
			configuration: RaptorDIBuilder.() -> Unit,
		): RaptorDI {
			val parentContext = context.parent
			val parentDI = when (context) {
				is RaptorContext.Lazy -> parentContext?.di
				else -> context.di
			}

			val contextModule = Module(
				name = "raptor (context)",
				providers = listOf(RaptorDI.provider(key) {
					// TODO Looks type-unsafe.
					@Suppress("UNCHECKED_CAST")
					((context as? RaptorContext.Lazy)?.context ?: context) as Context
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
		override val providers: List<RaptorDI.Provider<*>>,
	) : RaptorDI.Module


	internal class Provider<Value>(
		override val key: RaptorDIKey<Value>,
		private val provide: RaptorDI.() -> Value?,
	) : RaptorDI.Provider<Value> {

		override fun provide(di: RaptorDI) =
			with(di) { provide() }
	}
}
