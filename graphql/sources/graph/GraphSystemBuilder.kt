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
		private val interfaceDefinitionsByValueClass: MutableMap<KClass<*>, GraphInterfaceDefinition<*>> = mutableMapOf()
		private var isComplete = false
		private val gqlTypes: MutableList<GType> = mutableListOf()
		private val operationDefinitionsByType: MutableMap<GraphOperation.Type, MutableMap<String, GraphOperationDefinition<*>>> = mutableMapOf()
		private val typeDefinitionExtensionKey = TypeDefinitionExtensionKey()
		private val typeDefinitionsByName: MutableMap<String, GraphNamedTypeDefinition<*>> = hashMapOf()
		private val typeDefinitionsByValueClass: MutableMap<KClass<*>, GraphTypeDefinition<*>> = hashMapOf()


		private fun applyAliasDefinition(definition: GraphAliasDefinition<*, *>) {
			when (val referencedDefinition = typeDefinitionsByValueClass[definition.referencedValueClass]) {
				null ->
					error("No GraphQL type definition was provided for ${definition.referencedValueClass} referenced by ${definition.valueClass}:\n$definition")

				is GraphAliasDefinition<*, *> ->
					error("GraphQL alias ${definition.valueClass} cannot reference alias ${definition.referencedValueClass}:\n" +
						"Alias: $definition\n" +
						"Referenced alias: $referencedDefinition")
			}
		}


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
						name = argument.name!!, // FIXME
						type = resolveTypeRef(argument.valueType, referee = definition),
						extensions = mapOf(
							typeDefinitionExtensionKey to resolveTypeDefinition(argument.valueType, referee = definition)
						)
					)
				},
				description = definition.description,
				name = definition.name,
				parseValue = { arguments ->
					GraphInputContext(arguments = arguments).useBlocking {
						with(definition) {
							with(environment as RaptorGraphScope) { construct() }
						}
					}
				}
			)
		}


		private fun applyInterfaceDefinition(definition: GraphInterfaceDefinition<*>) {
			gqlTypes += GInterfaceType(
				description = definition.description,
				fields = definition.fields.map { field ->
					GFieldDefinition(
						argumentDefinitions = field.arguments.map { argument ->
							GFieldArgumentDefinition(
								defaultValue = argument.default,
								description = null, // FIXME
								name = argument.name!!, // FIXME
								type = resolveTypeRef(argument.valueType, referee = definition)
							)
						},
						description = field.description,
						name = field.name,
						type = resolveTypeRef(field.valueType, referee = definition)
					)
				},
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
			gqlTypes += GObjectType(
				description = definition.description,
				fields = definition.fields.map { field ->
					GFieldDefinition(
						argumentDefinitions = field.arguments.map { argument ->
							GFieldArgumentDefinition(
								defaultValue = argument.default,
								description = null, // FIXME
								name = argument.name!!, // FIXME
								type = resolveTypeRef(argument.valueType, referee = definition),
								extensions = mapOf(
									typeDefinitionExtensionKey to resolveTypeDefinition(argument.valueType, referee = definition)
								)
							)
						},
						description = field.description,
						name = field.name,
						type = resolveTypeRef(field.valueType, referee = definition),
						extensions = mapOf(
							fieldDefinitionExtensionKey to field,
							typeDefinitionExtensionKey to resolveTypeDefinition(field.valueType, referee = definition)
						)
					)
				},
				interfaces = interfaceTypeRefsForObjectValueClass(definition.valueClass),
				kotlinType = definition.valueClass,
				name = definition.name
			)
		}


		private fun applyOperationDefinitions(definitions: Collection<GraphOperationDefinition<*>>, operationType: GraphOperation.Type) {
			gqlTypes += GObjectType(
				fields = definitions.map { definition ->
					GFieldDefinition(
						argumentDefinitions = definition.field.arguments.map { argument ->
							GFieldArgumentDefinition(
								defaultValue = argument.default,
								description = null, // FIXME
								name = argument.name!!, // FIXME
								type = resolveTypeRef(argument.valueType, referee = definition),
								extensions = mapOf(
									typeDefinitionExtensionKey to resolveTypeDefinition(argument.valueType, referee = definition)
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


		fun complete(): GraphSystem {
			check(!isComplete) { "complete() can only be called once." }
			isComplete = true

			for (definition in typeDefinitionsByValueClass.values)
				if (definition is GraphAliasDefinition<*, *>)
					applyAliasDefinition(definition)

			for (definition in typeDefinitionsByName.values)
				applyNamedTypeDefinition(definition)

			for ((operationType, definitionsByName) in operationDefinitionsByType)
				applyOperationDefinitions(definitionsByName.values, operationType = operationType)

			return GraphSystem(
				fieldDefinitionExtensionKey = fieldDefinitionExtensionKey,
				schema = GSchema(GDocument(definitions = gqlTypes)),
				typeDefinitionExtensionKey = typeDefinitionExtensionKey
			)
		}


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
				is GraphOperationDefinition<*> ->
					registerOperation(definition = definition)

				is GraphTypeDefinition<*> ->
					registerType(definition = definition)
			}
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
				Collection::class, List::class, Set::class -> resolveTypeDefinition(valueClassRef.arguments.first(), referee = referee)
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
