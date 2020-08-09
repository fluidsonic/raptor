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

	private val dependenciesByType: MutableMap<KType, Any?> = hashMapOf()
	private val currentlyResolvingTypes = mutableListOf<KType>()
	private val lock = ReentrantLock()


	@Suppress("NAME_SHADOWING")
	override fun get(type: KType): Any? =
		lock.withLock { // TODO Add fast-path if dependency is already resolved.
			val isNullable = type.isMarkedNullable
			val type = type.withNullability(false)
			val dependency = dependenciesByType.getOrPut(type) {
				if (currentlyResolvingTypes.contains(type))
					reportCyclicDependency(type)

				currentlyResolvingTypes += type

				try {
					return@getOrPut resolve(type, isNullable = isNullable)
				}
				finally {
					currentlyResolvingTypes.removeLast()
				}
			}

			if (dependency == null && !isNullable)
				reportUnexpectedNullDependency(type)

			dependency
		}


	override fun <Value> invoke(factory: RaptorDI.() -> Value) =
		lazy { factory() }


	private fun resolve(type: KType, isNullable: Boolean): Any? {
		dependenciesByModule.forEach { (module, moduleDependenciesByType) ->
			val provide = module.provideByType.entries.firstOrNull { it.key.isSubtypeOf(type) }?.value ?: return@forEach
			val dependency = provide()

			moduleDependenciesByType[type] = dependency

			return dependency
		}

		parent?.let { parent ->
			return parent.get(type)
		}

		if (isNullable) return null // TODO Remember unsuccessful resolutions to not perform them over and over again.
		else reportUnresolvedDependency(type)
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


	private fun reportUnresolvedDependency(type: KType): Nothing {
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
}
