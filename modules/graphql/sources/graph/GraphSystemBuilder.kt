package io.fluidsonic.raptor

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.GraphSystem.*
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
			GraphId.graphDefinition(),
			Int.graphDefinition(),
			String.graphDefinition()
		)
	}


	private class Build {

		private val fieldDefinitionExtensionKey = FieldDefinitionExtensionKey()
		private var optionalArgumentDirective: GDirective? = null
		private val interfaceDefinitionsByValueClass: MutableMap<KClass<*>, GraphInterfaceDefinition<*>> = mutableMapOf()
		private val interfaceExtensionDefinitionsByValueClass: MutableMap<KClass<*>, MutableList<GraphInterfaceExtensionDefinition<*>>> = mutableMapOf()
		private var isComplete = false
		private val gqlTypes: MutableList<GType> = mutableListOf()
		private val objectExtensionDefinitionsByValueClass: MutableMap<KClass<*>, MutableList<GraphObjectExtensionDefinition<*>>> = mutableMapOf()
		private val operationDefinitionsByType: MutableMap<RaptorGraphOperationType, MutableMap<String, GraphOperationDefinition<*>>> = mutableMapOf()
		private val typeDefinitionExtensionKey = TypeDefinitionExtensionKey()
		private val typeDefinitionsByName: MutableMap<String, GraphNamedTypeDefinition<*>> = hashMapOf()
		private val typeDefinitionsByValueClass: MutableMap<KClass<*>, GraphTypeDefinition<*>> = hashMapOf()
		private val valueTypeExtensionKey = ValueTypeExtensionKey()


		private fun applyEnumDefinition(definition: GraphEnumDefinition<*>) {
			gqlTypes += GEnumType(
				description = definition.description,
				name = definition.name,
				parseValue = { value ->
					(value as? String)?.let { name -> definition.values.firstOrNull { it.name == name } }
				},
				serializeValue = { (it as? Enum<*>)?.name },
				values = definition.values.map { value ->
					GEnumValueDefinition(
						name = value.name
					)
				}
			)
		}


		private fun applyInputObjectDefinition(definition: GraphInputObjectDefinition<*>) {
			gqlTypes += GInputObjectType(
				argumentDefinitions = definition.arguments.map { argument ->
					GInputObjectArgumentDefinition(
						defaultValue = argument.default,
						description = null, // FIXME
						directives = directivesForArgument(argument),
						name = argument.name!!, // FIXME
						type = resolveTypeRef(argument.valueType, referee = definition),
						extensions = mapOf(
							typeDefinitionExtensionKey to resolveTypeDefinition(argument.valueType, referee = definition),
							valueTypeExtensionKey to argument.valueType
						)
					)
				},
				description = definition.description,
				name = definition.name,
				parseValue = { arguments ->
					GraphInputContext(arguments = arguments).useBlocking {
						with(definition) {
							with(environment as RaptorGraphScope) { factory() }
						}
					}
				}
			)
		}


		private fun applyInterfaceDefinition(definition: GraphInterfaceDefinition<*>) {
			val definitionsByFieldName: MutableMap<String, RaptorGraphDefinition> = mutableMapOf()
			val fields = mutableListOf<GFieldDefinition>()

			for (fieldDefinition in definition.fields)
				fields += GFieldDefinition(
					argumentDefinitions = fieldDefinition.arguments.map { argument ->
						GFieldArgumentDefinition(
							defaultValue = argument.default,
							description = null, // FIXME
							directives = directivesForArgument(argument),
							name = argument.name!!, // FIXME
							type = resolveTypeRef(argument.valueType, referee = definition)
						)
					},
					description = fieldDefinition.description,
					name = fieldDefinition.name,
					type = resolveTypeRef(fieldDefinition.valueType, referee = definition)
				)

			for (extension in objectExtensionDefinitionsByValueClass[definition.valueClass].orEmpty())
				for (fieldDefinition in extension.fields) {
					definitionsByFieldName.put(fieldDefinition.name, extension)?.let { previousDefinition ->
						error(
							"An extension defines a duplicate field '${fieldDefinition.name}' on interface type '${definition.name}'.\n" +
								"$extension\n" +
								"Previous: $previousDefinition"
						)
					}

					fields += GFieldDefinition(
						argumentDefinitions = fieldDefinition.arguments.map { argument ->
							GFieldArgumentDefinition(
								defaultValue = argument.default,
								description = null, // FIXME
								directives = directivesForArgument(argument),
								name = argument.name!!, // FIXME
								type = resolveTypeRef(argument.valueType, referee = extension)
							)
						},
						description = fieldDefinition.description,
						name = fieldDefinition.name,
						type = resolveTypeRef(fieldDefinition.valueType, referee = extension)
					)
				}

			gqlTypes += GInterfaceType(
				description = definition.description,
				fields = fields,
				name = definition.name
			)
		}


		private fun applyNamedTypeDefinition(definition: GraphNamedTypeDefinition<*>) {
			when (definition) {
				is GraphEnumDefinition<*> ->
					applyEnumDefinition(definition)

				is GraphInputObjectDefinition<*> ->
					applyInputObjectDefinition(definition)

				is GraphInterfaceDefinition<*> ->
					applyInterfaceDefinition(definition)

				is GraphObjectDefinition<*> ->
					applyObjectDefinition(definition)

				is GraphScalarDefinition<*> ->
					applyScalarDefinition(definition)
			}
		}


		private fun applyObjectDefinition(definition: GraphObjectDefinition<*>) {
			val definitionsByFieldName: MutableMap<String, RaptorGraphDefinition> = mutableMapOf()
			val fields = mutableListOf<GFieldDefinition>()

			for (fieldDefinition in definition.fields)
				fields += GFieldDefinition(
					argumentDefinitions = fieldDefinition.arguments.map { argument ->
						GFieldArgumentDefinition(
							defaultValue = argument.default,
							description = null, // FIXME
							directives = directivesForArgument(argument),
							name = argument.name!!, // FIXME
							type = resolveTypeRef(argument.valueType, referee = definition),
							extensions = mapOf(
								typeDefinitionExtensionKey to resolveTypeDefinition(argument.valueType, referee = definition),
								valueTypeExtensionKey to argument.valueType
							)
						)
					},
					description = fieldDefinition.description,
					name = fieldDefinition.name,
					type = resolveTypeRef(fieldDefinition.valueType, referee = definition),
					extensions = mapOf(
						fieldDefinitionExtensionKey to fieldDefinition,
						typeDefinitionExtensionKey to resolveTypeDefinition(fieldDefinition.valueType, referee = definition)
					)
				)

			for (extension in objectExtensionDefinitionsByValueClass[definition.valueClass].orEmpty())
				for (fieldDefinition in extension.fields) {
					definitionsByFieldName.put(fieldDefinition.name, extension)?.let { previousDefinition ->
						error(
							"An extension defines a duplicate field '${fieldDefinition.name}' on interface type '${definition.name}'.\n" +
								"$extension\n" +
								"Previous: $previousDefinition"
						)
					}

					fields += GFieldDefinition(
						argumentDefinitions = fieldDefinition.arguments.map { argument ->
							GFieldArgumentDefinition(
								defaultValue = argument.default,
								description = null, // FIXME
								directives = directivesForArgument(argument),
								name = argument.name!!, // FIXME
								type = resolveTypeRef(argument.valueType, referee = extension),
								extensions = mapOf(
									typeDefinitionExtensionKey to resolveTypeDefinition(argument.valueType, referee = extension),
									valueTypeExtensionKey to argument.valueType
								)
							)
						},
						description = fieldDefinition.description,
						name = fieldDefinition.name,
						type = resolveTypeRef(fieldDefinition.valueType, referee = extension),
						extensions = mapOf(
							fieldDefinitionExtensionKey to fieldDefinition,
							typeDefinitionExtensionKey to resolveTypeDefinition(fieldDefinition.valueType, referee = definition)
						)
					)
				}

			gqlTypes += GObjectType(
				description = definition.description,
				fields = fields,
				interfaces = interfaceTypeRefsForObjectValueClass(definition.valueClass),
				kotlinType = definition.valueClass,
				name = definition.name
			)
		}


		private fun applyOperationDefinitions(definitions: Collection<GraphOperationDefinition<*>>, operationType: RaptorGraphOperationType) {
			gqlTypes += GObjectType(
				fields = definitions.map { definition ->
					GFieldDefinition(
						argumentDefinitions = definition.field.arguments.map { argument ->
							GFieldArgumentDefinition(
								defaultValue = argument.default,
								description = null, // FIXME
								directives = directivesForArgument(argument),
								name = argument.name!!, // FIXME
								type = resolveTypeRef(argument.valueType, referee = definition),
								extensions = mapOf(
									typeDefinitionExtensionKey to resolveTypeDefinition(argument.valueType, referee = definition),
									valueTypeExtensionKey to argument.valueType
								)
							)
						},
						description = definition.field.description,
						name = definition.field.name,
						type = resolveTypeRef(definition.field.valueType, referee = definition),
						extensions = mapOf(
							fieldDefinitionExtensionKey to definition.field,
							typeDefinitionExtensionKey to resolveTypeDefinition(definition.field.valueType, referee = definition)
						)
					)
				},
				name = operationType.gqlType.defaultObjectTypeName
			)
		}


		private fun applyScalarDefinition(definition: GraphScalarDefinition<*>) {
			gqlTypes += GCustomScalarType(
				description = definition.description,
				name = definition.name,
				parseValue = { value ->
					with(definition) {
						with(environment as RaptorGraphScope) {
							definition.jsonInputClass.isInstance(value).thenTake { parseJson(value) }
						}
					}
				},
				parseValueNode = { value ->
					with(environment as RaptorGraphScope) {
						when (value) {
							is GBooleanValue -> definition.parseBoolean?.let { it(value.value) }
							is GFloatValue -> definition.parseFloat?.let { it(value.value) }
							is GIntValue -> definition.parseInt?.let { it(value.value) }
							is GObjectValue -> definition.parseObject?.let { it(value.unwrap()) }
							is GStringValue -> definition.parseString?.let { it(value.value) }
							else -> null
						}
					}
				},
				serializeValue = { value ->
					with(definition as GraphScalarDefinition<Any>) {
						with(environment as RaptorGraphScope) {
							serializeJson(value)
						}
					}
				}
			)
		}


		private fun checkAliasDefinition(definition: GraphAliasDefinition<*, *>) {
			when (val referencedDefinition = typeDefinitionsByValueClass[definition.referencedValueClass]) {
				null ->
					error("No GraphQL type definition was provided for ${definition.referencedValueClass} referenced by ${definition.valueClass}:\n$definition")

				is GraphAliasDefinition<*, *> ->
					error("GraphQL alias ${definition.valueClass} cannot reference alias ${definition.referencedValueClass}:\n" +
						"Alias: $definition\n" +
						"Referenced alias: $referencedDefinition")
			}
		}


		fun complete(): GraphSystem {
			check(!isComplete) { "complete() can only be called once." }
			isComplete = true

			for (definition in typeDefinitionsByValueClass.values)
				if (definition is GraphAliasDefinition<*, *>)
					checkAliasDefinition(definition)

			for (definition in typeDefinitionsByName.values)
				applyNamedTypeDefinition(definition)

			for ((operationType, definitionsByName) in operationDefinitionsByType)
				applyOperationDefinitions(definitionsByName.values, operationType = operationType)

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

			return GraphSystem(
				fieldDefinitionExtensionKey = fieldDefinitionExtensionKey,
				schema = GSchema(GDocument(definitions = definitions)),
				typeDefinitionExtensionKey = typeDefinitionExtensionKey,
				valueTypeExtensionKey = valueTypeExtensionKey
			)
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

				is GraphTypeDefinition<*> ->
					registerType(definition = definition)
			}

			for (additionalDefinition in definition.additionalDefinitions)
				register(additionalDefinition)
		}


		private fun registerOperation(definition: GraphOperationDefinition<*>) {
			val operationDefinitionsByName = operationDefinitionsByType
				.getOrPut(definition.type) { mutableMapOf() }

			val name = definition.field.name

			operationDefinitionsByName[name]?.let { existingDefinition ->
				error(
					"More than one GraphQL ${definition.type} operation definition has been provided with name '$name':\n" +
						"1st: $existingDefinition\n" +
						"2nd: $definition"
				)
			}

			operationDefinitionsByName[name] = definition
		}


		private fun registerType(definition: GraphTypeDefinition<*>) {
			typeDefinitionsByValueClass[definition.valueClass]?.let { existingDefinition ->
				error(
					"More than one GraphQL type definition has been provided for ${definition.valueClass}:\n" +
						"1st: $existingDefinition\n" +
						"2nd: $definition"
				)
			}

			if (definition is GraphNamedTypeDefinition<*>) {
				when (definition.name) {
					GSpecification.defaultMutationTypeName,
					GSpecification.defaultQueryTypeName,
					GSpecification.defaultSubscriptionTypeName ->
						error("GraphQL type definition must not use the operation type name '${definition.name}':\n$definition")
				}

				typeDefinitionsByName[definition.name]?.let { existingDefinition ->
					error(
						"More than one GraphQL type definition has been provided with type name '${definition.name}':\n" +
							"1st: $existingDefinition\n" +
							"2nd: $definition"
					)
				}

				typeDefinitionsByName[definition.name] = definition
			}

			if (definition is GraphInterfaceDefinition<*>)
				interfaceDefinitionsByValueClass[definition.valueClass] = definition

			typeDefinitionsByValueClass[definition.valueClass] = definition
		}


		private fun resolveTypeDefinition(valueClass: KClass<*>, referee: RaptorGraphDefinition): GraphTypeDefinition<*> =
			typeDefinitionsByValueClass[valueClass]
				?: error("GraphQL type definition was not provided for $valueClass referenced by:\n$referee")


		private fun resolveTypeDefinition(valueClassRef: KType, referee: RaptorGraphDefinition): GraphTypeDefinition<*> =
			when (val classifier = valueClassRef.classifier) {
				Collection::class, List::class, Maybe::class, Set::class -> resolveTypeDefinition(valueClassRef.arguments.first(), referee = referee)
				is KClass<*> -> resolveTypeDefinition(classifier, referee = referee)
				is KTypeParameter -> error("A type parameter '$valueClassRef' is not representable in GraphQL:\n$referee")
				else -> error("The type reference '$valueClassRef' is not representable in GraphQL:\n$referee")
			}


		private fun resolveTypeDefinition(valueClassProjection: KTypeProjection, referee: RaptorGraphDefinition): GraphTypeDefinition<*> {
			val type = valueClassProjection.type ?: error("A star projection cannot be represented in GraphQL:\n$referee")
			// FIXME check variance

			return resolveTypeDefinition(type, referee = referee)
		}


		private fun resolveTypeRef(valueClass: KClass<*>, referee: RaptorGraphDefinition): GNamedTypeRef =
			when (val definition = typeDefinitionsByValueClass[valueClass]) {
				is GraphAliasDefinition<*, *> ->
					if (definition.isId)
						GIdTypeRef
					else
						resolveTypeRef(definition.referencedValueClass, referee = definition)

				is GraphNamedTypeDefinition<*> ->
					GNamedTypeRef(definition.name)

				null ->
					error("GraphQL type definition was not provided for $valueClass referenced by:\n$referee")
			}


		// FIXME 'Set' won't work like this
		// FIXME support other collection types & find generic approach?
		private fun resolveTypeRef(valueClassRef: KType, referee: RaptorGraphDefinition): GTypeRef {
			val typeRef = when (val classifier = valueClassRef.classifier) {
				Maybe::class -> return resolveTypeRef(valueClassRef.arguments.first(), referee = referee)
				Collection::class, List::class, Set::class -> GListTypeRef(resolveTypeRef(valueClassRef.arguments.first(), referee = referee))
				is KClass<*> -> resolveTypeRef(classifier, referee = referee)
				is KTypeParameter -> error("A type parameter '$valueClassRef' is not representable in GraphQL:\n$referee")
				else -> error("The type reference '$valueClassRef' is not representable in GraphQL:\n$referee")
			}

			return if (valueClassRef.isMarkedNullable) typeRef else GNonNullTypeRef(typeRef)
		}


		private fun resolveTypeRef(valueClassProjection: KTypeProjection, referee: RaptorGraphDefinition): GTypeRef {
			val type = valueClassProjection.type ?: error("A star projection cannot be represented in GraphQL:\n$referee")
			// FIXME check variance

			return resolveTypeRef(type, referee = referee)
		}
	}
}
