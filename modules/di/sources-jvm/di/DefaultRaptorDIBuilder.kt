package io.fluidsonic.raptor.di

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.RaptorDI.*
import kotlin.reflect.*
import kotlin.reflect.full.*


internal class DefaultRaptorDIBuilder : RaptorDIBuilder {

	private val providers: MutableList<Provider<*>> = mutableListOf()


	fun createModule(name: String) =
		RaptorDI.module(name = name, providers = providers)


	// TODO Split into provide & provideOptional.
	override fun <Value : Any> provide(key: RaptorDIKey<in Value>, provide: RaptorDI.() -> Value?) {
		validateKey(key)

		providers += RaptorDI.provider(key = key, provide = provide)
	}


	private fun validateKey(key: RaptorDIKey<*>) {
		if (key is KTypeDIKey<*>)
			validateType(key.type)
	}


	private fun validateType(type: KType) {
		require(!type.isMarkedNullable) { "Cannot provide a dependency for nullable type '$type'." }

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
