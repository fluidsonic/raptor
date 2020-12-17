package io.fluidsonic.raptor.graphql.internal

import io.fluidsonic.stdlib.*
import kotlin.reflect.*


internal data class KotlinType(
	val classifier: KClass<*>,
	val typeArgument: KotlinType? = null,
	val isNullable: Boolean,
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


	fun specialize(): KotlinType {
		if (isSpecialized)
			return this // TODO ok?

		return when (val typeArgument = typeArgument) {
			null -> withTypeArgument(of(
				type = classifier.typeParameters.single().upperBounds.single(),
				containingType = null,
				allowMaybe = false,
				allowNull = true,
				allowedVariance = KVariance.OUT,
				requireSpecialization = true
			))
			else -> withTypeArgument(typeArgument.specialize())
		}
	}


	fun specialize(typeArgument: KotlinType): KotlinType {
		if (isSpecialized)
			return this // TODO ok?

		return when (val thisTypeArgument = this.typeArgument) {
			null -> withTypeArgument(typeArgument)
			else -> thisTypeArgument.specialize(typeArgument) // FIXME correct?
		}
	}


	override fun toString() = buildString {
		append(classifier.qualifiedName ?: "(unnamed class)")
		typeArgument?.let { typeArgument ->
			append("<")
			append(typeArgument)
			append(">")
		}
		if (isNullable)
			append("?")
	}


	fun withNullable(isNullable: Boolean) = when (isNullable) {
		this.isNullable -> this
		else -> copy(isNullable = isNullable)
	}


	fun withTypeArgument(typeArgument: KotlinType?) =
		copy(typeArgument = typeArgument)


	fun withoutTypeArgument() =
		copy(typeArgument = null)


	companion object {

		fun of(
			type: KType,
			containingType: KotlinType?,
			allowMaybe: Boolean,
			allowNull: Boolean,
			allowedVariance: KVariance,
			requireSpecialization: Boolean,
		): KotlinType =
			of(
				type = type,
				containingType = containingType,
				allowMaybe = allowMaybe,
				allowNull = allowNull,
				allowedTypeParameterName = containingType?.classifier?.typeParameters?.singleOrNull()?.name,
				allowedVariance = allowedVariance,
				requireSpecialization = requireSpecialization,
				rootType = type
			) ?: error("Type '$type' is not valid here.")


		private fun of(
			type: KType,
			containingType: KotlinType?,
			allowMaybe: Boolean,
			allowNull: Boolean,
			allowedTypeParameterName: String?,
			allowedVariance: KVariance,
			requireSpecialization: Boolean,
			rootType: KType,
		): KotlinType? {
			if (!allowNull && type.isMarkedNullable)
				error("Type '$rootType' cannot be used here. The Kotlin type must not be nullable.")
			if (!allowMaybe && type.classifier == Maybe::class)
				error("Type '$rootType' cannot be used here. 'Maybe' is not allowed in this context.")
			if (type.arguments.size > 1)
				error("Type '$rootType' cannot be used here. Kotlin types with more than one type parameter are not supported.")

			fun checkTypeVariance(variance: KVariance?) {
				when (variance) {
					KVariance.INVARIANT -> Unit
					else -> check(variance == allowedVariance) {
						val varianceString = when (allowedVariance) {
							KVariance.INVARIANT -> "invariant"
							KVariance.IN -> "'in'"
							KVariance.OUT -> "'out'"
						}

						"Type '$rootType' cannot be used here. Type arguments in this position must have $varianceString variance."
					}
				}
			}

			return when (val classifier = type.classifier) {
				is KClass<*> -> when (classifier) {
					Any::class, Nothing::class ->
						error("Type '$rootType' cannot be used here. '${classifier.simpleName}' cannot be used for GraphQL type mapping.")

					else -> KotlinType(
						classifier = classifier,
						isNullable = type.isMarkedNullable,
						typeArgument = type.arguments.firstOrNull()?.let { projection ->
							checkTypeVariance(projection.variance ?: classifier.typeParameters.first().variance)

							when (val argument = projection.type) {
								null -> null
								else -> of(
									type = argument,
									containingType = containingType,
									allowMaybe = false,
									allowNull = allowNull,
									allowedTypeParameterName = allowedTypeParameterName,
									allowedVariance = allowedVariance,
									requireSpecialization = requireSpecialization,
									rootType = rootType
								)
							}
						}
					)
				}

				is KTypeParameter -> {
					checkTypeVariance(classifier.variance)

					if (allowedTypeParameterName != null && classifier.name == allowedTypeParameterName)
						null
					else
						error("Type '$rootType' cannot be used here. Type parameters must resolve to the type parameter of the containing type.")
				}

				else -> error("Type '$rootType' cannot be used here.")
			}
		}
	}
}
