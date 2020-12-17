package io.fluidsonic.raptor

import java.util.concurrent.locks.*
import kotlin.collections.set
import kotlin.concurrent.*
import kotlin.reflect.*
import kotlin.reflect.full.*


internal class DefaultRaptorDI(
	modules: List<DefaultRaptorDIModule>,
	private val parent: RaptorDI?,
) : RaptorDI {

	private val dependenciesByModule: MutableMap<DefaultRaptorDIModule, MutableMap<KType, Any?>> = modules
		.reversed()
		.associateWithTo(mutableMapOf()) { hashMapOf() }

	private val dependenciesByType: MutableMap<KType, ResolvedDependency> = hashMapOf()
	private val currentlyResolvingTypes = mutableListOf<KType>()
	private val lock = ReentrantLock()


	@Suppress("NAME_SHADOWING")
	override fun get(type: KType): Any? {
		val isNullable = type.isMarkedNullable
		val type = type.withNullability(false)

		val value = getDependency(type).value
		if (value == null && !isNullable)
			reportMissingDependency(type)

		return value
	}


	private fun getDependency(type: KType): ResolvedDependency =
		lock.withLock { // TODO Add fast-path if dependency is already resolved.
			dependenciesByType.getOrPut(type) {
				if (currentlyResolvingTypes.contains(type))
					reportCyclicDependency(type)

				currentlyResolvingTypes += type

				try {
					return@getOrPut resolve(type)
				}
				finally {
					currentlyResolvingTypes.removeLast()
				}
			}
		}


	override fun <Value> invoke(factory: RaptorDI.() -> Value) =
		lazy { factory() }


	private fun resolve(type: KType): ResolvedDependency {
		// FIXME if not successful must delegate to parents first before going on with modules
		dependenciesByType.values.forEach { resolvedDependency ->
			// FIXME must set moduleDependenciesByType[…]
			if (resolvedDependency.type.isSubtypeOf(type))
				return resolvedDependency
		}

		dependenciesByModule.forEach { (module, moduleDependenciesByType) ->
			val (providedType, provide) = module.provideByType.entries.firstOrNull { it.key.isSubtypeOf(type) } ?: return@forEach
			val value = provide()

			moduleDependenciesByType[type] = value

			return ResolvedDependency(type = providedType, value = value)
		}

		parent?.let { parent ->
			return (parent as DefaultRaptorDI).getDependency(type) // FIXME make abstract
		}

		return ResolvedDependency(type = type, value = null)
	}


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
			append(this) // FIXME will print wrong DI
		})
	}


	private fun reportUnexpectedNullDependency(type: KType): Nothing {
		error("Dependency of type '$type' is not available. It resolved to 'null' which was not expected.\n\nDI:\n$this") // FIXME will print wrong DI
	}


	private fun reportMissingDependency(type: KType): Nothing {
		error("Cannot resolve dependency of type '$type'.\n\nDI:\n$this") // FIXME will print wrong DI
	}


	override fun toString() = buildString {
		val processedTypes = hashSetOf<KType>()

		dependenciesByModule.forEach { (module, moduleDependenciesByType) ->
			append(module.name)
			append(" {")

			if (module.provideByType.isNotEmpty()) {
				append("\n")
				module.provideByType
					.keys
					.map { type -> type to type.toString() }
					.sortedBy { it.second }
					.forEach { (type, typeString) ->
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


	private class ResolvedDependency(
		val type: KType,
		val value: Any?,
	)
}
