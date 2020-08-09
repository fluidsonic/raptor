package io.fluidsonic.raptor

import kotlin.reflect.*
import kotlin.reflect.full.*


internal class DefaultRaptorDIBuilder : RaptorDIBuilder {

	private val providerByKey: MutableMap<KType, RaptorDI.() -> Any> = hashMapOf()


	fun createModule(name: String) =
		DefaultRaptorDIModule(
			name = name,
			provideByType = HashMap(providerByKey)
		)


	override fun provide(type: KType, provide: RaptorDI.() -> Any) {
		validateType(type)

		@Suppress("NAME_SHADOWING")
		val type = type.withNullability(false)

		providerByKey.put(type, provide)?.let {
			error("A dependency for type '$type' has already been registered.")
		}
	}


	private fun validateType(type: KType) {
		when (val classifier = type.classifier) {
			Any::class, Unit::class -> error("Cannot provide a dependency for type '$type'.")

			is KClass<*> -> when {
				classifier.isSubclassOf(RaptorContext::class) ->
					error("Cannot provide a dependency for a subclass of type '${RaptorContext::class.qualifiedName}'.")

				else -> validateType(type = type, rootType = type)
			}

			else -> validateType(type = type, rootType = type)
		}

		type.arguments.forEach { validateTypeArgument(it, rootType = type) }
	}


	private fun validateType(type: KType, rootType: KType) {
		when (val classifier = type.classifier) {
			is KClass<*> -> Unit
			is KTypeParameter ->
				error("Cannot provide a dependency for a type with type parameter as type arguments: $rootType")

			else -> error("Unknown type classifier: $classifier")
		}

		type.arguments.forEach { validateTypeArgument(it, rootType = type) }
	}


	private fun validateTypeArgument(projection: KTypeProjection, rootType: KType) {
		projection.type?.let { validateType(type = it, rootType = rootType) }
	}
}
