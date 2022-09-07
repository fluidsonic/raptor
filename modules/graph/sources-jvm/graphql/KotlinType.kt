package io.fluidsonic.raptor.graph

import io.fluidsonic.stdlib.*
import kotlin.reflect.*


internal data class KotlinType(
	val classifier: KClass<*>,
	val typeArguments: List<KotlinType?> = emptyList(),
	val isNullable: Boolean,
) {

	init {
		require(classifier.typeParameters.size == typeArguments.size) {
			"Unexpected number of type arguments for classifier '${classifier.qualifiedName}': " +
				"expected ${classifier.typeParameters.size}, got ${typeArguments.size}"
		}
	}


	val isGeneric: Boolean
		get() = classifier == Any::class || classifier.typeParameters.isNotEmpty()


	val isSpecialized: Boolean
		get() = !isGeneric || (typeArguments.isNotEmpty() && typeArguments.all { it != null && it.isSpecialized })


	fun specialize(): KotlinType {
		if (isSpecialized)
			return this // TODO ok?

		if (classifier == Any::class)
			error("Cannot specialize this type.")

		return withTypeArguments(typeArguments.mapIndexed { index, typeArgument ->
			when (typeArgument) {
				null -> of(
					type = classifier.typeParameters[index].upperBounds.single(),
					containingType = null,
					allowMaybe = false,
					allowNull = true,
					allowedVariance = KVariance.OUT,
					requireSpecialization = true
				)

				else -> typeArgument.specialize()
			}
		})
	}


	fun specialize(typeArguments: List<KotlinType>): KotlinType {
		if (isSpecialized)
			return this // TODO ok?

		if (classifier == Any::class) {
			val typeArgument = typeArguments.singleOrNull()
			require(typeArgument != null) { "Expected exactly 1 type argument but got ${typeArguments.size}." }

			return KotlinType(classifier = typeArgument.classifier, isNullable = isNullable || typeArgument.isNullable)
		}

		// FIXME specialize arguments
		return withTypeArguments(typeArguments)

		// FIXME
//		return when (val thisTypeArgument = this.typeArgument) {
//			null -> withTypeArgument(typeArgument)
//			else -> thisTypeArgument.specialize(typeArgument) // FIXME correct?
//		}
	}


	override fun toString() = buildString {
		append(classifier.qualifiedName ?: "(unnamed class)")
		if (typeArguments.isNotEmpty())
			typeArguments.joinTo(this, prefix = "<", separator = ", ", postfix = ">")
		if (isNullable)
			append("?")
	}


	fun withNullable(isNullable: Boolean) = when (isNullable) {
		this.isNullable -> this
		else -> copy(isNullable = isNullable)
	}


	fun withTypeArguments(typeArguments: List<KotlinType>) =
		copy(typeArguments = typeArguments)


	fun withoutTypeArguments() =
		copy(typeArguments = typeArguments.map { null })


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
			)// ?: error("Type '$type' is not valid here.")
				?: KotlinType(classifier = Any::class, isNullable = type.isMarkedNullable) // FIXME so many hacks… basically means totally generic type, e.g. <Value>


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
						typeArguments = type.arguments.map { projection ->
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
