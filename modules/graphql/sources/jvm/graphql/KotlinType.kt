package io.fluidsonic.raptor.graphql.internal

import io.fluidsonic.stdlib.*
import kotlin.reflect.*


internal data class KotlinType(
	val classifier: KClass<*>,
	val typeArgument: KotlinType? = null,
) {

	init {
		when (classifier.typeParameters.size) {
			0 -> require(typeArgument == null) { "Non-generic classifier '${classifier.qualifiedName}' cannot have type argument '$typeArgument'." }
			1 -> Unit
			else -> error("Classifier '${classifier.qualifiedName}' must not have multiple type parameters.")
		}
	}


	val isGeneric: Boolean
		get() = classifier.typeParameters.isNotEmpty()


	val isSpecialized: Boolean
		get() = !isGeneric || (typeArgument?.isSpecialized ?: false)


	fun specialize(typeArgument: KClass<*>): KotlinType {
		check(!isSpecialized)

		return when (val thisTypeArgument = this.typeArgument) {
			null -> withTypeArgument(typeArgument)
			else -> thisTypeArgument.specialize(typeArgument)
		}
	}


	override fun toString() = when (typeArgument) {
		null -> classifier.qualifiedName ?: "(unnamed class)"
		else -> "${classifier.qualifiedName ?: "(unnamed class)"}<$typeArgument>"
	}


	fun withTypeArgument(classifier: KClass<*>?) =
		withTypeArgument(classifier?.let { KotlinType(classifier = it) })


	fun withTypeArgument(typeArgument: KotlinType?) =
		copy(typeArgument = typeArgument)


	fun withoutTypeArgument() =
		copy(typeArgument = null)


	companion object {

		fun of(
			type: KType,
			requireSpecialization: Boolean,
			allowMaybe: Boolean,
			allowNull: Boolean,
		): KotlinType =
			of(type = type, requireSpecialization = requireSpecialization, allowMaybe = allowMaybe, allowNull = allowNull, rootType = type)


		// FIXME check variance
		private fun of(
			type: KType,
			requireSpecialization: Boolean,
			allowMaybe: Boolean,
			allowNull: Boolean,
			rootType: KType,
		): KotlinType {
			if (!allowNull && type.isMarkedNullable)
				error("Type '$rootType' cannot be used here. The Kotlin type must not be nullable.")
			if (!allowMaybe && type.classifier == Maybe::class)
				error("Type '$rootType' cannot be used here. 'Maybe' is not allowed in this context.")
			if (type.arguments.size > 1)
				error("Type '$rootType' cannot be used here. Kotlin types with more than one type parameter are not supported.")

			return when (val classifier = type.classifier) {
				is KClass<*> -> when (classifier) {
					Any::class, Nothing::class ->
						error("Type '$rootType' cannot be used here. '${classifier.simpleName}' cannot be used for GraphQL type mapping.")

					else -> KotlinType(
						classifier = classifier,
						typeArgument = type.arguments.firstOrNull()?.let { projection ->
							when (val argument = projection.type) {
								null -> null
								else -> of(
									type = argument,
									requireSpecialization = requireSpecialization,
									allowMaybe = false,
									allowNull = allowNull,
									rootType = rootType
								)
							}
						}
					)
				}

				else -> error("Type '$rootType' cannot be used here.")
			}
		}
	}
}
