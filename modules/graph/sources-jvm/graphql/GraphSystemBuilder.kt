package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*
import kotlin.reflect.*
import kotlin.reflect.full.*


internal class GraphSystemBuilder private constructor(
	private val typeSystem: GraphTypeSystem,
) {

	private val interfaceTypesByKotlinType = typeSystem.types
		.filterIsInstance<InterfaceGraphType>()
		.associateBy { it.kotlinType }


	private fun build() =
		buildSchema()


	private fun buildSchema(): GSchema {
		val typeDefinitions = buildTypeDefinitions()
		checkQueryRootTypeIsProvided(typeDefinitions)

		val schema = GSchema(document = GDocument(definitions = typeDefinitions))
		schema.assertValid()

		return schema
	}


	private fun buildEnumDefinition(type: EnumGraphType): GEnumType {
		val coercer = EnumCoercer(type)

		return GEnumType(
			description = type.description,
			name = type.name,
			values = type.values
				.map { value ->
					GEnumValueDefinition(
						description = value.description,
						name = value.name,
					)
				}
				.sortedBy { it.name },
			extensions = GNodeExtensionSet {
				inputLiteralCoercer = coercer
				outputValueCoercer = coercer
				inputValueCoercer = coercer
			}
		)
	}


	private fun buildFieldArgumentDefinition(argument: GraphArgument) = GFieldArgumentDefinition(
		defaultValue = argument.defaultValue,
		description = argument.description,
		name = argument.name,
		type = typeRef(argument.kotlinType, isInput = true),
		extensions = GNodeExtensionSet {
			raptorType = underlyingType(argument.kotlinType, isInput = true)
		}
	)


	private fun buildFieldDefinition(field: GraphField) = GFieldDefinition(
		argumentDefinitions = field.arguments.map(::buildFieldArgumentDefinition).sortedBy { it.name },
		description = field.description,
		name = field.name,
		type = typeRef(field.kotlinType, isInput = false),
		extensions = GNodeExtensionSet {
			raptorField = field
			raptorType = underlyingType(field.kotlinType, isInput = false)
			resolver = FieldResolver
		}
	)


	private fun buildInputObjectArgumentDefinition(argument: GraphArgument) = GInputObjectArgumentDefinition(
		defaultValue = argument.defaultValue,
		description = argument.description,
		name = argument.name,
		type = typeRef(argument.kotlinType, isInput = true),
		extensions = GNodeExtensionSet {
			raptorType = underlyingType(argument.kotlinType, isInput = true)
		}
	)


	private fun buildInputObjectDefinition(type: InputObjectGraphType): GInputObjectType {
		val argumentDefinitions = type.arguments.map(::buildInputObjectArgumentDefinition).sortedBy { it.name }
		val coercer = InputObjectCoercer(argumentDefinitions = argumentDefinitions, raptorType = type)

		return GInputObjectType(
			argumentDefinitions = argumentDefinitions,
			description = type.description,
			name = type.name,
			extensions = GNodeExtensionSet {
				inputLiteralCoercer = coercer
				inputValueCoercer = coercer
			}
		)
	}


	private fun buildInterfaceDefinition(type: InterfaceGraphType) = GInterfaceType(
		description = type.description,
		fieldDefinitions = type.fields.map(::buildFieldDefinition).sortedBy { it.name },
		name = type.name,
	)


	private fun buildObjectDefinition(type: ObjectGraphType) = GObjectType(
		description = type.description,
		fieldDefinitions = type.fields.map(::buildFieldDefinition).sortedBy { it.name },
		interfaces = interfaceTypeRefsForKotlinType(type.kotlinType).sortedBy { it.name },
		name = type.name,
		extensions = GNodeExtensionSet {
			kotlinType = type.kotlinType.classifier
		}
	)


	private fun buildScalarDefinition(type: ScalarGraphType): GCustomScalarType {
		val coercer = ScalarCoercer(type)

		return GCustomScalarType(
			description = type.description,
			name = type.name,
			extensions = GNodeExtensionSet {
				inputLiteralCoercer = coercer
				outputValueCoercer = coercer
				inputValueCoercer = coercer
			}
		)
	}


	private fun buildTypeDefinition(type: NamedGraphType) = when (type) {
		is EnumGraphType -> buildEnumDefinition(type)
		is InputObjectGraphType -> buildInputObjectDefinition(type)
		is InterfaceGraphType -> buildInterfaceDefinition(type)
		is ObjectGraphType -> buildObjectDefinition(type)
		is ScalarGraphType -> buildScalarDefinition(type)
		is UnionGraphType -> buildUnionDefinition(type)
	}


	private fun buildTypeDefinitions(): List<GNamedType> =
		typeSystem.types
			.filterIsInstance<NamedGraphType>()
			.onEach(::checkTypeNameIsNotReserved)
			.map(::buildTypeDefinition)
			.sortedBy { it.name }


	private fun buildUnionDefinition(type: UnionGraphType) = GUnionType(
		description = type.description,
		name = type.name,
		possibleTypes = resolvePossibleTypesForKotlinType(type.kotlinType),
	)


	// Asserts on the emitted type definitions rather than on the declared operation types: the query root type being
	// present is the actual requirement, and any future route that contributes it must satisfy this check too.
	// `assertValid()` would catch this as well, but only with fluid GraphQL's `Query root type must be provided.`, which
	// names neither raptor's DSL nor the graph that lacks the operation.
	private fun checkQueryRootTypeIsProvided(typeDefinitions: List<GNamedType>) {
		if (typeDefinitions.none { it is GObjectType && it.name == GLanguage.defaultQueryTypeName })
			error(
				"A GraphQL schema must provide a query root type, so a graph must define at least one query operation.\n" +
					"Add one to this graph, for example:\n" +
					"graphOperationDefinition<String>(name = \"hello\", operationType = RaptorGraphOperationType.query) { … }\n---"
			)
	}


	// Applied to every type the type system holds: those are exactly the types raptor emits, so raptor's own mappings
	// for the built-in scalar names — which fluid GraphQL declares instead — never reach this check and need no
	// exemption from it.
	// Unlike `GraphSystemDefinitionBuilder`, this stage no longer holds the definition or its stack trace — only the
	// type — so the Kotlin type is the best definition-site locator available.
	private fun checkTypeNameIsNotReserved(type: NamedGraphType) {
		if (GLanguage.isReservedTypeName(type.name))
			error("A GraphQL type definition must not use the reserved type name '${type.name}':\n${type.kotlinType}\n---")
	}


	private fun interfaceTypeRefsForKotlinType(kotlinType: KotlinType): List<GNamedTypeRef> {
		val typeNames = mutableSetOf<String>()
		interfaceTypeNamesForObjectValueClass(kotlinType, target = typeNames)

		return typeNames.map(::GNamedTypeRef)
	}


	private fun interfaceTypeNamesForObjectValueClass(kotlinType: KotlinType, target: MutableSet<String>) {
		for (superType in kotlinType.classifier.supertypes) {
			val superClass = superType.classifier as? KClass<*> ?: continue
			val superKotlinType = KotlinType(
				classifier = superClass,
				isNullable = false,
				typeArguments = superClass.typeParameters.map { parameter ->
					(parameter.upperBounds.singleOrNull()?.classifier as? KClass<*>)?.let { classifier ->
						KotlinType(classifier, isNullable = false, typeArguments = classifier.typeParameters.map { null })
					}
				},
			)

			val gqlSuperClassName = interfaceTypesByKotlinType[superKotlinType]?.name
			if (gqlSuperClassName !== null)
				target += gqlSuperClassName

			interfaceTypeNamesForObjectValueClass(superKotlinType, target = target)
		}
	}


	private fun resolvePossibleTypesForKotlinType(kotlinType: KotlinType): List<GNamedTypeRef> =
		when (kotlinType.classifier) {
			RaptorUnion2::class -> listOf(
				// TODO This is a hack.
				GNamedTypeRef(((typeSystem.resolveOutputType(kotlinType.typeArguments[0]!!)
					?: error("Cannot resolve GraphQL type for Kotlin type '${kotlinType.typeArguments[0]}'.")) as NamedGraphType).name),
				GNamedTypeRef(((typeSystem.resolveOutputType(kotlinType.typeArguments[1]!!)
					?: error("Cannot resolve GraphQL type for Kotlin type '${kotlinType.typeArguments[1]}'.")) as NamedGraphType).name),
			)

			else -> typeSystem.types
				.filterIsInstance<ObjectGraphType>()
				.filterNot { it.kotlinType.isGeneric }
				.filter { it.kotlinType.classifier.isSubclassOf(kotlinType.classifier) }
				.ifEmpty { error("Cannot find any possible types for union type '$kotlinType'.") }
				.map { it.name }
				.sorted()
				.map(::GNamedTypeRef)
		}


	private fun typeRef(kotlinType: KotlinType, isInput: Boolean): GTypeRef {
		val nonNullKotlinType = kotlinType.withNullable(false)

		return when (nonNullKotlinType.classifier) {
			Collection::class, List::class, Set::class -> // TODO improve
				GListTypeRef(typeRef(checkNotNull(nonNullKotlinType.typeArguments.single()), isInput = isInput))

			else -> when (
				val type = when (isInput) {
					true -> typeSystem.resolveInputType(nonNullKotlinType)
					false -> typeSystem.resolveOutputType(nonNullKotlinType)
				}
			) {
				is AliasGraphType -> when {
					type.isId -> GIdTypeRef
					else -> typeRef(type.referencedKotlinType, isInput = isInput).nullableRef
				}

				is NamedGraphType ->
					GNamedTypeRef(type.name)

				// A type declared outside raptor has no entry among the emitted types, so only its name is available.
				null -> GNamedTypeRef(
					typeSystem.resolveExternallyDeclaredTypeName(nonNullKotlinType)
						?: error("Cannot resolve GraphQL type for Kotlin type '$nonNullKotlinType'.") // TODO print stacktrace of usage(s) here
				)
			}
		}.let { typeRef ->
			when (kotlinType.isNullable) {
				true -> typeRef
				false -> GNonNullTypeRef(typeRef)
			}
		}
	}


	// `null` for a type declared outside raptor, which raptor has no type of its own for. Callers that also need the
	// type to exist go through `typeRef`, which rejects a Kotlin type with no mapping at all.
	private fun underlyingType(kotlinType: KotlinType, isInput: Boolean): GraphType? {
		@Suppress("NAME_SHADOWING")
		val kotlinType = kotlinType.withNullable(false)

		return when (kotlinType.classifier) {
			Collection::class, List::class, Set::class -> // TODO improve
				underlyingType(checkNotNull(kotlinType.typeArguments.single()), isInput = isInput)

			else -> when (isInput) {
				true -> typeSystem.resolveInputType(kotlinType)
				false -> typeSystem.resolveOutputType(kotlinType)
			}
		}
	}


	companion object {

		fun build(typeSystem: GraphTypeSystem): GSchema =
			GraphSystemBuilder(typeSystem = typeSystem).build()
	}
}
