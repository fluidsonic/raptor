package io.fluidsonic.raptor.graphql.internal

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.*
import io.fluidsonic.stdlib.*
import kotlin.reflect.*


// FIXME check duplicate op field names
internal class GraphSystemDefinitionBuilder private constructor(
	private val initialDefinitions: Collection<RaptorGraphDefinition>,
) {

	private val aliasTypeDefinition: MutableCollection<AliasGraphTypeDefinition> = mutableListOf()
	private val definitionsToScanForGenericTypeReferences: MutableList<GraphTypeSystemDefinition> = mutableListOf()
	private val inputTypeDefinitionRegistry = TypeDefinitionRegistry(inputOrOutput = "input")
	private val operationDefinitionsByType: MutableMap<RaptorGraphOperationType, MutableMap<String, GraphOperationDefinition>> = mutableMapOf()
	private val outputTypeDefinitionRegistry = TypeDefinitionRegistry(inputOrOutput = "output")
	private val registeredDefinitions: MutableSet<RaptorGraphDefinition> = hashSetOf()
	private val typeDefinitionsByName: MutableMap<String, NamedGraphTypeDefinition> = hashMapOf()
	private val unspecializedDefinitionsByClassifier: MutableMap<KotlinType, MutableCollection<GraphTypeSystemDefinition>> = hashMapOf()

	private val interfaceTypeExtensionDefinitionsByKotlinType: MutableMap<KotlinType, MutableCollection<InterfaceExtensionGraphDefinition>> =
		hashMapOf()

	private val objectTypeExtensionDefinitionsByKotlinType: MutableMap<KotlinType, MutableCollection<ObjectExtensionGraphDefinition>> =
		hashMapOf()


	fun build(): GraphSystemDefinition {
		process()

		return GraphSystemDefinition(
			definitions = registeredDefinitions.mapNotNull { definition -> // FIXME refactor
				when (definition) {
					is GraphTypeSystemDefinition -> when (definition.kotlinType.isSpecialized) {
						true -> definition
						false -> {
							val typeArgument = definition.kotlinType.classifier.typeParameters.single().upperBounds.single()
							if (typeArgument.classifier == Any::class)
								return@mapNotNull null

							definition.specialize(
								typeArgument = KotlinType.of(
									type = typeArgument,
									containingType = null,
									allowMaybe = false,
									allowNull = true,
									allowedVariance = KVariance.OUT,
									requireSpecialization = true
								)!!, // FIXME
								namePrefix = ""
							)
						}
					}
					is GraphOperationDefinition -> definition
				}
			}
		)
	}


	private fun checkExtensionFieldNameCollisions() {
		for (definition in typeDefinitionsByName.values)
			when (definition) {
				is InterfaceGraphDefinition -> checkExtensionFieldNameCollisions(
					fieldDefinitions = definition.fieldDefinitions +
						interfaceTypeExtensionDefinitionsByKotlinType[definition.kotlinType]?.flatMap { it.fieldDefinitions }.orEmpty(),
					typeDefinition = definition,
					typeKind = "interface"
				)

				is ObjectGraphDefinition -> checkExtensionFieldNameCollisions(
					fieldDefinitions = definition.fieldDefinitions +
						objectTypeExtensionDefinitionsByKotlinType[definition.kotlinType]?.flatMap { it.fieldDefinitions }.orEmpty(),
					typeDefinition = definition,
					typeKind = "object"
				)

				else ->
					Unit
			}
	}


	private fun checkExtensionFieldNameCollisions(
		fieldDefinitions: Collection<GraphFieldDefinition>,
		typeDefinition: NamedGraphTypeDefinition,
		typeKind: String,
	) {
		val fieldDefinitionsByName = hashMapOf<String, GraphFieldDefinition>()
		for (fieldDefinition in fieldDefinitions)
			fieldDefinitionsByName.put(fieldDefinition.name, fieldDefinition)?.let { existingFieldDefinition ->
				error(
					"An extension defines a duplicate field '${fieldDefinition.name}' on $typeKind type '${typeDefinition.name}'.\n" +
						"$fieldDefinition\n" +
						"Previous: $existingFieldDefinition"
				)
			}
	}


	private fun process() {
		registerDefinitions(defaultDefinitions)
		registerDefinitions(initialDefinitions)
		resolveAliasTypeDefinitions()
		resolveAllReferences()

		checkExtensionFieldNameCollisions()
	}


	private fun registerAliasDefinition(definition: AliasGraphTypeDefinition) {
		aliasTypeDefinition += definition
	}


	private fun registerDefinition(definition: RaptorGraphDefinition) {
		if (!registeredDefinitions.add(definition))
			return

		when (definition) {
			is GraphOperationDefinition -> registerOperationDefinition(definition)
			is GraphTypeSystemDefinition -> registerTypeSystemDefinition(definition)
		}

		registerDefinitions(definition.additionalDefinitions)
	}


	private fun registerDefinitions(definitions: Collection<RaptorGraphDefinition>) {
		for (definition in definitions)
			registerDefinition(definition)
	}


	private fun registerInterfaceExtensionDefinition(definition: InterfaceExtensionGraphDefinition) {
		interfaceTypeExtensionDefinitionsByKotlinType.getOrPut(definition.kotlinType, ::mutableListOf).add(definition)
	}


	private fun registerNamedTypeDefinition(definition: NamedGraphTypeDefinition) {
		if (!definition.kotlinType.isSpecialized)
			return

		when (definition.name) {
			GLanguage.defaultMutationTypeName,
			GLanguage.defaultQueryTypeName,
			GLanguage.defaultSubscriptionTypeName,
			->
				error("A GraphQL type definition must not use the default operation type name '${definition.name}':\n$definition\n---")
		}

		typeDefinitionsByName.put(definition.name, definition)?.let { existingDefinition ->
			error(
				"More than one GraphQL type definition has been provided with type name '${definition.name}':\n" +
					"1st: $existingDefinition\n" +
					"2nd: $definition\n---"
			)
		}
	}


	private fun registerObjectExtensionDefinition(definition: ObjectExtensionGraphDefinition) {
		objectTypeExtensionDefinitionsByKotlinType.getOrPut(definition.kotlinType, ::mutableListOf).add(definition)
	}


	private fun registerOperationDefinition(definition: GraphOperationDefinition) {
		operationDefinitionsByType
			.getOrPut(definition.operationType, ::hashMapOf)
			.put(definition.fieldDefinition.name, definition)
			?.let { existingDefinition ->
				error(
					"More than one GraphQL ${definition.operationType} operation definition has been provided " +
						"with name '${definition.fieldDefinition.name}':\n" +
						"1st: $existingDefinition\n" +
						"2nd: $definition\n---"
				)
			}
	}


	private fun registerTypeDefinition(definition: GraphTypeDefinition) {
		if (definition.isInput)
			inputTypeDefinitionRegistry.register(definition)

		if (definition.isOutput)
			outputTypeDefinitionRegistry.register(definition)

		when (definition) {
			is AliasGraphTypeDefinition -> registerAliasDefinition(definition)
			is NamedGraphTypeDefinition -> registerNamedTypeDefinition(definition)
		}
	}


	private fun registerTypeExtensionDefinition(definition: GraphTypeExtensionDefinition) {
		when (definition) {
			is InterfaceExtensionGraphDefinition -> registerInterfaceExtensionDefinition(definition)
			is ObjectExtensionGraphDefinition -> registerObjectExtensionDefinition(definition)
		}
	}


	private fun registerTypeSystemDefinition(definition: GraphTypeSystemDefinition) {
		definitionsToScanForGenericTypeReferences += definition

		if (!definition.kotlinType.isSpecialized)
			unspecializedDefinitionsByClassifier.getOrPut(definition.kotlinType, ::mutableListOf).add(definition)

		when (definition) {
			is GraphTypeDefinition -> registerTypeDefinition(definition)
			is GraphTypeExtensionDefinition -> registerTypeExtensionDefinition(definition)
		}
	}


	private fun resolveAliasTypeDefinition(definition: AliasGraphTypeDefinition) {
		val inputDefinition = definition.isInput.thenTake {
			inputTypeDefinitionRegistry.resolve(definition.referencedKotlinType, referee = definition)
		}
		val outputDefinition = definition.isOutput.thenTake {
			outputTypeDefinitionRegistry.resolve(definition.referencedKotlinType, referee = definition)
		}

		if (inputDefinition == null && outputDefinition == null) {
			val inputOrOutput = when {
				definition.isInput && definition.isOutput -> "input or output"
				definition.isInput -> "input"
				definition.isOutput -> "output"
				else -> error("Definition is neither input nor output type.")
			}

			error(
				"No GraphQL $inputOrOutput type definition was provided for Kotlin type '${definition.referencedKotlinType}' " +
					"referenced by Kotlin type '${definition.kotlinType}':\n" +
					"$definition\n---"
			)
		}

		if (inputDefinition is AliasGraphTypeDefinition)
			error(
				"GraphQL alias Kotlin type '${definition.kotlinType}' cannot reference alias Kotlin type '${definition.referencedKotlinType}':\n" +
					"Alias definition: $definition\n" +
					"Referenced alias: $inputDefinition\n---"
			)

		if (outputDefinition is AliasGraphTypeDefinition)
			error(
				"GraphQL alias Kotlin type '${definition.kotlinType}' cannot reference alias Kotlin type '${definition.referencedKotlinType}':\n" +
					"Alias: $definition\n" +
					"Referenced alias: $outputDefinition\n---"
			)
	}


	private fun resolveAliasTypeDefinitions() {
		for (definition in aliasTypeDefinition)
			resolveAliasTypeDefinition(definition)
	}


	private tailrec fun resolveAllReferences() {
		specializeForGenericTypeReferences(definitionsToScanForGenericTypeReferences.removeLastOrNull() ?: return)
		resolveAllReferences()

		// Recursive because specialization may add new type definitions as needed.
	}


	private fun specializeForGenericTypeReferences(definition: GraphTypeSystemDefinition) {
		when (definition) {
			is AliasGraphTypeDefinition -> Unit
			is EnumGraphDefinition -> Unit
			is ScalarGraphDefinition -> Unit

			is InputObjectGraphDefinition -> definition.argumentDefinitions.forEach { argument ->
				inputTypeDefinitionRegistry.resolve(argument.kotlinType, referee = argument)
			}

			is InterfaceGraphDefinition -> definition.fieldDefinitions.forEach { field ->
				outputTypeDefinitionRegistry.resolve(field.kotlinType, referee = field)
			}

			is InterfaceExtensionGraphDefinition -> {
				outputTypeDefinitionRegistry.resolve(definition.kotlinType, referee = definition)

				definition.fieldDefinitions.forEach { field ->
					outputTypeDefinitionRegistry.resolve(field.kotlinType, referee = field)
				}
			}

			is ObjectGraphDefinition -> definition.fieldDefinitions.forEach { field ->
				outputTypeDefinitionRegistry.resolve(field.kotlinType, referee = field)
			}

			is ObjectExtensionGraphDefinition -> {
				outputTypeDefinitionRegistry.resolve(definition.kotlinType, referee = definition)

				definition.fieldDefinitions.forEach { field ->
					outputTypeDefinitionRegistry.resolve(field.kotlinType, referee = field)
				}
			}
		}
	}


	private fun specializeGenericTypeDefinition(kotlinType: KotlinType, typeArgument: KotlinType, argumentType: GraphTypeDefinition) {
		argumentType as NamedGraphTypeDefinition // FIXME

		val definitions = checkNotNull(unspecializedDefinitionsByClassifier[kotlinType.withNullable(false)])
		for (definition in definitions)
			registerDefinition(definition.specialize(typeArgument = typeArgument, namePrefix = argumentType.name))
	}


	companion object {

		private val defaultDefinitions: Collection<RaptorGraphDefinition> = listOf(
			Boolean.graphDefinition(),
			Double.graphDefinition(),
			GraphId.graphDefinition(),
			Int.graphDefinition(),
			String.graphDefinition()
		)


		fun build(definitions: Collection<RaptorGraphDefinition>) =
			GraphSystemDefinitionBuilder(initialDefinitions = definitions).build()
	}


	private inner class TypeDefinitionRegistry(
		private val inputOrOutput: String,
	) {

		private val definitionsByKotlinType: MutableMap<KotlinType, GraphTypeDefinition> = hashMapOf()


		fun register(definition: GraphTypeDefinition) {
			definitionsByKotlinType.put(definition.kotlinType, definition)?.let { existingDefinition ->
				error(
					"More than one GraphQL $inputOrOutput type definition has been provided for Kotlin type '${definition.kotlinType}':\n" +
						"1st: $existingDefinition\n" +
						"2nd: $definition\n---"
				)
			}
		}


		// FIXME handle alias and generic alias argument
		fun resolve(kotlinType: KotlinType, referee: RaptorGraphNode): GraphTypeDefinition? {
			@Suppress("NAME_SHADOWING")
			val kotlinType = kotlinType.withNullable(false)

			definitionsByKotlinType[kotlinType]
				?.let { return it }

			// FIXME nesting
			val typeArgument = kotlinType.typeArgument ?: run {
				check(!unspecializedDefinitionsByClassifier.containsKey(kotlinType)) {
					"A GraphQL definition cannot reference the generic Kotlin type '$kotlinType' without specifying a type argument:\n" +
						"$referee\n---"
				}

				return null
			}

			val kotlinTypeWithoutTypeArgument = kotlinType.withoutTypeArgument()
			val genericDefinition = definitionsByKotlinType[kotlinTypeWithoutTypeArgument]
				?: return null

			check(genericDefinition.kotlinType.isGeneric) {
				"A GraphQL definition cannot specify a type argument when referencing the non-generic Kotlin type '$kotlinTypeWithoutTypeArgument':\n" +
					"$referee\n---"
			}

			val argumentType = resolve(typeArgument, referee = referee)
				?: return null

			specializeGenericTypeDefinition(kotlinTypeWithoutTypeArgument, typeArgument = typeArgument, argumentType = argumentType)

			return checkNotNull(definitionsByKotlinType[kotlinType])
		}
	}
}
