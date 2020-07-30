package io.fluidsonic.raptor

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.graphql.internal.*
import io.fluidsonic.stdlib.*
import kotlin.reflect.*


// FIXME pre-validation for better error messages & add more context to existing errors
// FIXME configurable enum values
// FIXME add support for directives
// FIXME don't add unreferenced default type descriptors?
// FIXME check duplicate op field names
// FIXME auto-register ID types? (BSON)
internal class GraphSystemBuilder {

	private val definitions: MutableList<RaptorGraphDefinition> = defaultDefinitions.toMutableList()


	fun build(): GraphSystem {
		val build = Build()

		for (definition in definitions)
			build.register(definition)

		return build.complete()
	}


	fun add(definition: RaptorGraphDefinition) {
		check(definitions.none { it === definition }) { "Cannot add the same definition multiple times: $definition" }

		definitions += definition
	}


	companion object {

		private val defaultDefinitions: List<RaptorGraphDefinition> = listOf(
			Boolean.graphDefinition(),
			Double.graphDefinition(),
			//GraphId.graphDefinition(), // FIXME
			Int.graphDefinition(),
			String.graphDefinition()
		)
	}


	private class Build {

		private val aliasDefinitions: MutableList<GraphAliasDefinition<*, *>> = mutableListOf()
		private var optionalArgumentDirective: GDirective? = null
		private val inputTypeDefinitionsByValueClass: MutableMap<KClass<*>, GraphTypeDefinition<*>> = hashMapOf()
		private val interfaceDefinitionsByValueClass: MutableMap<KClass<*>, GraphInterfaceDefinition<*>> = mutableMapOf()
		private val interfaceExtensionDefinitionsByValueClass: MutableMap<KClass<*>, MutableList<GraphInterfaceExtensionDefinition<*>>> = mutableMapOf()
		private var isComplete = false
		private val gqlTypes: MutableList<GType> = mutableListOf()
		private val objectExtensionDefinitionsByValueClass: MutableMap<KClass<*>, MutableList<GraphObjectExtensionDefinition<*>>> = mutableMapOf()
		private val operationDefinitionsByType: MutableMap<RaptorGraphOperationType, MutableMap<String, GraphOperationDefinition<*>>> = mutableMapOf()
		private val outputTypeDefinitionsByValueClass: MutableMap<KClass<*>, GraphTypeDefinition<*>> = hashMapOf()
		private val typeDefinitionsByName: MutableMap<String, GraphNamedTypeDefinition<*>> = hashMapOf()


		private fun completeEnumDefinition(definition: GraphEnumDefinition<*>) {
			gqlTypes += GEnumType(
				description = definition.description,
				name = definition.name,
				values = definition.values.map { GEnumValueDefinition(name = it.name) },
				extensions = GNodeExtensionSet {
					outputCoercer = EnumCoercer
					raptorTypeDefinition = definition
					variableInputCoercer = EnumCoercer
				}
			)
		}


		private fun completeInputObjectDefinition(definition: GraphInputObjectDefinition<*>) {
			gqlTypes += GInputObjectType(
				argumentDefinitions = definition.arguments.map { createInputObjectArgumentDefinition(definition = it, referee = definition) },
				description = definition.description,
				name = definition.name,
				extensions = GNodeExtensionSet {
					raptorTypeDefinition = definition
					variableInputCoercer = InputObjectCoercer
				}
			)
		}


		private fun completeInterfaceDefinition(definition: GraphInterfaceDefinition<*>) {
			val definitionsByFieldName: MutableMap<String, RaptorGraphDefinition> = mutableMapOf()
			val fields = mutableListOf<GFieldDefinition>()

			for (fieldDefinition in definition.fields)
				fields += createFieldDefinition(definition = fieldDefinition, referee = definition)

			for (extensionDefinition in objectExtensionDefinitionsByValueClass[definition.valueClass].orEmpty())
				for (fieldDefinition in extensionDefinition.fields) {
					definitionsByFieldName.put(fieldDefinition.name, extensionDefinition)?.let { previousDefinition ->
						error(
							"An extension defines a duplicate field '${fieldDefinition.name}' on interface type '${definition.name}'.\n" +
								"$extensionDefinition\n" +
								"Previous: $previousDefinition"
						)
					}

					fields += createFieldDefinition(definition = fieldDefinition, referee = extensionDefinition)
				}

			gqlTypes += GInterfaceType(
				description = definition.description,
				fields = fields,
				name = definition.name,
				extensions = GNodeExtensionSet {
					raptorTypeDefinition = definition
				}
			)
		}


		private fun completeNamedTypeDefinition(definition: GraphNamedTypeDefinition<*>) {
			when (definition) {
				is GraphEnumDefinition<*> ->
					completeEnumDefinition(definition)

				is GraphInputObjectDefinition<*> ->
					completeInputObjectDefinition(definition)

				is GraphInterfaceDefinition<*> ->
					completeInterfaceDefinition(definition)

				is GraphObjectDefinition<*> ->
					completeObjectDefinition(definition)

				is GraphScalarDefinition<*> ->
					completeScalarDefinition(definition)
			}
		}


		private fun completeObjectDefinition(definition: GraphObjectDefinition<*>) {
			val definitionsByFieldName: MutableMap<String, RaptorGraphDefinition> = mutableMapOf()
			val fields = mutableListOf<GFieldDefinition>()

			for (fieldDefinition in definition.fields)
				fields += createFieldDefinition(definition = fieldDefinition, referee = definition)

			for (extensionDefinition in objectExtensionDefinitionsByValueClass[definition.valueClass].orEmpty())
				for (fieldDefinition in extensionDefinition.fields) {
					definitionsByFieldName.put(fieldDefinition.name, extensionDefinition)?.let { previousDefinition ->
						error(
							"An extension defines a duplicate field '${fieldDefinition.name}' on interface type '${definition.name}'.\n" +
								"$extensionDefinition\n" +
								"Previous: $previousDefinition"
						)
					}

					fields += createFieldDefinition(definition = fieldDefinition, referee = extensionDefinition)
				}

			gqlTypes += GObjectType(
				description = definition.description,
				fields = fields,
				interfaces = interfaceTypeRefsForObjectValueClass(definition.valueClass),
				name = definition.name,
				extensions = GNodeExtensionSet {
					raptorTypeDefinition = definition
				}
			)
		}


		private fun completeOperationDefinitions(definitions: Collection<GraphOperationDefinition<*>>, operationType: RaptorGraphOperationType) {
			gqlTypes += GObjectType(
				fields = definitions.map { createFieldDefinition(definition = it.field, referee = it) },
				name = operationType.gqlType.defaultObjectTypeName
			)
		}


		private fun completeScalarDefinition(definition: GraphScalarDefinition<*>) {
			gqlTypes += GCustomScalarType(
				description = definition.description,
				name = definition.name,
				extensions = GNodeExtensionSet {
					nodeInputCoercer = ScalarCoercer
					outputCoercer = ScalarCoercer
					variableInputCoercer = ScalarCoercer
				}
			)
		}


		private fun createFieldArgumentDefinition(
			definition: GraphArgumentDefinition<*>,
			referee: RaptorGraphDefinition
		): GFieldArgumentDefinition =
			GFieldArgumentDefinition(
				defaultValue = definition.default,
				description = null, // FIXME
				directives = directivesForArgument(definition),
				name = definition.name!!, // FIXME
				type = resolveTypeRef(
					valueClassRef = definition.valueType,
					referee = referee,
					typeDefinitionsByValueClass = inputTypeDefinitionsByValueClass
				),
				extensions = GNodeExtensionSet {
					raptorArgumentDefinition = definition
					raptorTypeDefinition = resolveTypeDefinition(
						valueClassRef = definition.valueType,
						referee = referee,
						typeDefinitionsByValueClass = inputTypeDefinitionsByValueClass
					)
				}
			)


		private fun createFieldDefinition(
			definition: GraphFieldDefinition<*, *>,
			referee: RaptorGraphDefinition
		): GFieldDefinition =
			GFieldDefinition(
				argumentDefinitions = definition.arguments.map { createFieldArgumentDefinition(definition = it, referee = referee) },
				description = definition.description,
				name = definition.name,
				type = resolveTypeRef(
					valueClassRef = definition.valueType,
					referee = referee,
					typeDefinitionsByValueClass = outputTypeDefinitionsByValueClass
				),
				extensions = GNodeExtensionSet {
					raptorFieldDefinition = definition
					raptorTypeDefinition = resolveTypeDefinition(
						valueClassRef = definition.valueType,
						referee = referee,
						typeDefinitionsByValueClass = outputTypeDefinitionsByValueClass
					)
				}
			)


		private fun createInputObjectArgumentDefinition(
			definition: GraphArgumentDefinition<*>,
			referee: RaptorGraphDefinition
		): GInputObjectArgumentDefinition =
			GInputObjectArgumentDefinition(
				defaultValue = definition.default,
				description = null, // FIXME
				directives = directivesForArgument(definition),
				name = definition.name!!, // FIXME
				type = resolveTypeRef(
					valueClassRef = definition.valueType,
					referee = referee,
					typeDefinitionsByValueClass = inputTypeDefinitionsByValueClass
				),
				extensions = GNodeExtensionSet {
					raptorArgumentDefinition = definition
					raptorTypeDefinition = resolveTypeDefinition(
						valueClassRef = definition.valueType,
						referee = referee,
						typeDefinitionsByValueClass = inputTypeDefinitionsByValueClass
					)
				}
			)


		fun complete(): GraphSystem {
			check(!isComplete) { "complete() can only be called once." }
			isComplete = true

			for (definition in aliasDefinitions)
				resolveAlias(definition)

			for (definition in typeDefinitionsByName.values)
				completeNamedTypeDefinition(definition)

			for ((operationType, definitionsByName) in operationDefinitionsByType)
				completeOperationDefinitions(definitionsByName.values, operationType = operationType)

			val definitions = gqlTypes.toMutableList<GTypeSystemDefinition>()

			optionalArgumentDirective?.let { directive ->
				definitions += GDirectiveDefinition(
					description = "An argument with this directive does not require a value. " +
						"Providing no value may lead to a different behavior than providing a null value.",
					name = directive.name,
					locations = setOf(GDirectiveLocation.ARGUMENT_DEFINITION)
				)
			}

			// FIXME error on extensions for non-existent types

			return GraphSystem(schema = GSchema(GDocument(definitions = definitions)))
		}


		private fun directivesForArgument(argument: GraphArgumentDefinition<*>) =
			if (argument.valueType.classifier == Maybe::class)
				listOf(optionalArgumentDirective ?: GDirective(name = "optional").also {
					optionalArgumentDirective = it
				})
			else
				emptyList()


		private fun interfaceTypeRefsForObjectValueClass(valueClass: KClass<*>): List<GNamedTypeRef> {
			val typeNames = mutableSetOf<String>()
			interfaceTypeNamesForObjectValueClass(valueClass, target = typeNames)

			return typeNames.map(::GNamedTypeRef)
		}


		private fun interfaceTypeNamesForObjectValueClass(valueClass: KClass<*>, target: MutableSet<String>) {
			for (superType in valueClass.supertypes) {
				val superClass = superType.classifier as? KClass<*> ?: continue

				val gqlSuperClassName = interfaceDefinitionsByValueClass[superClass]?.name
				if (gqlSuperClassName !== null)
					target += gqlSuperClassName

				interfaceTypeNamesForObjectValueClass(superClass, target = target)
			}
		}


		fun register(definition: RaptorGraphDefinition) {
			when (definition) {
				is GraphInterfaceExtensionDefinition<*> ->
					interfaceExtensionDefinitionsByValueClass.getOrPut(definition.valueClass) { mutableListOf() }
						.add(definition)

				is GraphObjectExtensionDefinition<*> ->
					objectExtensionDefinitionsByValueClass.getOrPut(definition.valueClass) { mutableListOf() }
						.add(definition)

				is GraphOperationDefinition<*> ->
					registerOperation(definition = definition)

				is GraphNamedTypeDefinition<*> ->
					registerType(definition = definition)

				is GraphAliasDefinition<*, *> ->
					registerAlias(definition = definition)
			}

			for (additionalDefinition in definition.additionalDefinitions)
				register(additionalDefinition)
		}


		private fun registerAlias(definition: GraphAliasDefinition<*, *>) {
			aliasDefinitions += definition
		}


		private fun registerOperation(definition: GraphOperationDefinition<*>) {
			val operationDefinitionsByName = operationDefinitionsByType
				.getOrPut(definition.type) { mutableMapOf() }

			val name = definition.field.name

			operationDefinitionsByName[name]?.let { existingDefinition ->
				error(
					"More than one GraphQL ${definition.type} operation definition has been provided with name '$name':\n" +
						"1st: $existingDefinition\n" +
						"2nd: $definition\n---"
				)
			}

			operationDefinitionsByName[name] = definition
		}


		private fun registerType(definition: GraphNamedTypeDefinition<*>) {
			if (definition.isInput)
				inputTypeDefinitionsByValueClass[definition.valueClass]?.let { existingDefinition ->
					error(
						"More than one GraphQL input type definition has been provided for ${definition.valueClass}:\n" +
							"1st: $existingDefinition\n" +
							"2nd: $definition\n---"
					)
				}
			if (definition.isOutput)
				outputTypeDefinitionsByValueClass[definition.valueClass]?.let { existingDefinition ->
					error(
						"More than one GraphQL output type definition has been provided for ${definition.valueClass}:\n" +
							"1st: $existingDefinition\n" +
							"2nd: $definition\n---"
					)
				}

			when (definition.name) {
				GLanguage.defaultMutationTypeName,
				GLanguage.defaultQueryTypeName,
				GLanguage.defaultSubscriptionTypeName ->
					error("GraphQL type definition must not use the operation type name '${definition.name}':\n$definition")
			}

			typeDefinitionsByName[definition.name]?.let { existingDefinition ->
				error(
					"More than one GraphQL type definition has been provided with type name '${definition.name}':\n" +
						"1st: $existingDefinition\n" +
						"2nd: $definition\n---"
				)
			}

			typeDefinitionsByName[definition.name] = definition

			if (definition is GraphInterfaceDefinition<*>)
				interfaceDefinitionsByValueClass[definition.valueClass] = definition

			if (definition.isInput)
				inputTypeDefinitionsByValueClass[definition.valueClass] = definition
			if (definition.isOutput)
				outputTypeDefinitionsByValueClass[definition.valueClass] = definition
		}


		private fun resolveAlias(definition: GraphAliasDefinition<*, *>) {
			val referencedInputTypeDefinition = inputTypeDefinitionsByValueClass[definition.referencedValueClass]
			val referencedOutputTypeDefinition = outputTypeDefinitionsByValueClass[definition.referencedValueClass]

			if (referencedInputTypeDefinition == null && referencedOutputTypeDefinition == null)
				error("No GraphQL type definition was provided for ${definition.referencedValueClass} referenced by ${definition.valueClass}:\n$definition")

			if (referencedInputTypeDefinition != null) {
				if (referencedInputTypeDefinition is GraphAliasDefinition<*, *>)
					error("GraphQL alias ${definition.valueClass} cannot reference alias ${definition.referencedValueClass}:\n" +
						"Alias: $definition\n" +
						"Referenced alias: $referencedInputTypeDefinition")

				inputTypeDefinitionsByValueClass[definition.valueClass]?.let { existingDefinition ->
					error(
						"More than one GraphQL input type definition has been provided for ${definition.valueClass}:\n" +
							"1st: $existingDefinition\n" +
							"2nd: $definition\n---"
					)
				}

				inputTypeDefinitionsByValueClass[definition.valueClass] = definition
			}

			if (referencedOutputTypeDefinition != null) {
				if (referencedOutputTypeDefinition is GraphAliasDefinition<*, *>)
					error("GraphQL alias ${definition.valueClass} cannot reference alias ${definition.referencedValueClass}:\n" +
						"Alias: $definition\n" +
						"Referenced alias: $referencedOutputTypeDefinition")

				outputTypeDefinitionsByValueClass[definition.valueClass]?.let { existingDefinition ->
					error(
						"More than one GraphQL output type definition has been provided for ${definition.valueClass}:\n" +
							"1st: $existingDefinition\n" +
							"2nd: $definition\n---"
					)
				}

				outputTypeDefinitionsByValueClass[definition.valueClass] = definition
			}
		}


		private fun resolveTypeDefinition(
			valueClass: KClass<*>,
			referee: RaptorGraphDefinition,
			typeDefinitionsByValueClass: Map<KClass<*>, GraphTypeDefinition<*>>
		): GraphTypeDefinition<*> =
			typeDefinitionsByValueClass[valueClass]
				?: error("GraphQL type definition was not provided for $valueClass referenced by:\n$referee\n---")


		private fun resolveTypeDefinition(
			valueClassRef: KType,
			referee: RaptorGraphDefinition,
			typeDefinitionsByValueClass: Map<KClass<*>, GraphTypeDefinition<*>>
		): GraphTypeDefinition<*> =
			when (val classifier = valueClassRef.classifier) {
				Collection::class, List::class, Maybe::class, Set::class -> resolveTypeDefinition(
					valueClassRef.arguments.first(),
					referee = referee,
					typeDefinitionsByValueClass = typeDefinitionsByValueClass
				)
				is KClass<*> -> resolveTypeDefinition(classifier, referee = referee, typeDefinitionsByValueClass = typeDefinitionsByValueClass)
				is KTypeParameter -> error("A type parameter '$valueClassRef' is not representable in GraphQL:\n$referee\n---")
				else -> error("The type reference '$valueClassRef' is not representable in GraphQL:\n$referee\n---")
			}


		private fun resolveTypeDefinition(
			valueClassProjection: KTypeProjection,
			referee: RaptorGraphDefinition,
			typeDefinitionsByValueClass: Map<KClass<*>, GraphTypeDefinition<*>>
		): GraphTypeDefinition<*> {
			val type = valueClassProjection.type ?: error("A star projection cannot be represented in GraphQL:\n$referee\n---")
			// FIXME check variance

			return resolveTypeDefinition(type, referee = referee, typeDefinitionsByValueClass = typeDefinitionsByValueClass)
		}


		private fun resolveTypeRef(
			valueClass: KClass<*>,
			referee: RaptorGraphDefinition,
			typeDefinitionsByValueClass: Map<KClass<*>, GraphTypeDefinition<*>>
		): GNamedTypeRef =
			when (val definition = typeDefinitionsByValueClass[valueClass]) {
				is GraphAliasDefinition<*, *> ->
					if (definition.isId)
						GIdTypeRef
					else
						resolveTypeRef(definition.referencedValueClass, referee = definition, typeDefinitionsByValueClass = typeDefinitionsByValueClass)

				is GraphNamedTypeDefinition<*> ->
					GNamedTypeRef(definition.name)

				null ->
					error("GraphQL type definition was not provided for $valueClass referenced by:\n$referee\n---")
			}


		// FIXME 'Set' won't work like this
		// FIXME support other collection types & find generic approach?
		private fun resolveTypeRef(
			valueClassRef: KType,
			referee: RaptorGraphDefinition,
			typeDefinitionsByValueClass: Map<KClass<*>, GraphTypeDefinition<*>>
		): GTypeRef {
			val typeRef = when (val classifier = valueClassRef.classifier) {
				Maybe::class -> return resolveTypeRef(
					valueClassRef.arguments.first(),
					referee = referee,
					typeDefinitionsByValueClass = typeDefinitionsByValueClass
				)
				Collection::class, List::class, Set::class -> GListTypeRef(resolveTypeRef(
					valueClassRef.arguments.first(),
					referee = referee,
					typeDefinitionsByValueClass = typeDefinitionsByValueClass
				))
				is KClass<*> -> resolveTypeRef(classifier, referee = referee, typeDefinitionsByValueClass = typeDefinitionsByValueClass)
				is KTypeParameter -> error("A type parameter '$valueClassRef' is not representable in GraphQL:\n$referee\n---")
				else -> error("The type reference '$valueClassRef' is not representable in GraphQL:\n$referee\n---")
			}

			return if (valueClassRef.isMarkedNullable) typeRef else GNonNullTypeRef(typeRef)
		}


		private fun resolveTypeRef(
			valueClassProjection: KTypeProjection,
			referee: RaptorGraphDefinition,
			typeDefinitionsByValueClass: Map<KClass<*>, GraphTypeDefinition<*>>
		): GTypeRef {
			val type = valueClassProjection.type ?: error("A star projection cannot be represented in GraphQL:\n$referee\n---")
			// FIXME check variance

			return resolveTypeRef(type, referee = referee, typeDefinitionsByValueClass = typeDefinitionsByValueClass)
		}
	}
}
