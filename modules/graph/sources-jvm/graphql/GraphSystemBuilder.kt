package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*
import io.fluidsonic.stdlib.*
import kotlin.reflect.*
import kotlin.reflect.full.*


internal class GraphSystemBuilder private constructor(
	private val tags: Set<Any>,
	private val typeSystem: GraphTypeSystem,
) {

	private val interfaceTypesByKotlinType = typeSystem.types
		.filterIsInstance<InterfaceGraphType>()
		.associateBy { it.kotlinType }


	private fun build() = DefaultRaptorGraph(schema = buildSchema(), tags = tags)


	// TODO validate
	private fun buildSchema() = GSchema(
		document = GDocument(definitions = buildDirectiveDefinitions() + buildTypeDefinitions()),
		supportOptional = true,
	)


	private fun buildDirectiveDefinitions(): List<GDirectiveDefinition> = buildList {
		val referencedDirectiveNames = findReferencedDirectiveNames()

		if (referencedDirectiveNames.contains(GraphDirective.optional.name))
			add(GDirectiveDefinition(
				description = "An argument with this directive does not require a value. " +
					"Providing no value may lead to a different behavior than providing a null value.",
				name = GraphDirective.optional.name,
				locations = setOf(GDirectiveLocation.ARGUMENT_DEFINITION, GDirectiveLocation.INPUT_FIELD_DEFINITION)
			))
	}


	private fun buildEnumDefinition(type: EnumGraphType) = GEnumType(
		description = type.description,
		name = type.name,
		values = type.values.map { GEnumValueDefinition(name = it) }.sortedBy { it.name },
		extensions = GNodeExtensionSet {
			outputCoercer = EnumCoercer
			nodeInputCoercer = EnumCoercer
			raptorType = type
			variableInputCoercer = EnumCoercer
		}
	)


	private fun buildFieldArgumentDefinition(argument: GraphArgument) = GFieldArgumentDefinition(
		defaultValue = argument.defaultValue,
		description = argument.description,
		directives = argument.directives.map { GDirective(name = it.name) }.sortedBy { it.name },
		name = argument.name,
		type = typeRef(argument.kotlinType, isInput = true),
		extensions = GNodeExtensionSet {
			raptorArgument = argument
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
		directives = argument.directives.map { GDirective(name = it.name) }.sortedBy { it.name },
		name = argument.name,
		type = typeRef(argument.kotlinType, isInput = true),
		extensions = GNodeExtensionSet {
			raptorArgument = argument
			raptorType = underlyingType(argument.kotlinType, isInput = true)
		}
	)


	private fun buildInputObjectDefinition(type: InputObjectGraphType) = GInputObjectType(
		argumentDefinitions = type.arguments.map(::buildInputObjectArgumentDefinition).sortedBy { it.name },
		description = type.description,
		name = type.name,
		extensions = GNodeExtensionSet {
			nodeInputCoercer = InputObjectCoercer
			raptorType = type
			variableInputCoercer = InputObjectCoercer
		}
	)


	private fun buildInterfaceDefinition(type: InterfaceGraphType) = GInterfaceType(
		description = type.description,
		fieldDefinitions = type.fields.map(::buildFieldDefinition).sortedBy { it.name },
		name = type.name,
		extensions = GNodeExtensionSet {
			raptorType = type
		}
	)


	private fun buildObjectDefinition(type: ObjectGraphType) = GObjectType(
		description = type.description,
		fieldDefinitions = type.fields.map(::buildFieldDefinition).sortedBy { it.name },
		interfaces = interfaceTypeRefsForKotlinType(type.kotlinType).sortedBy { it.name },
		name = type.name,
		extensions = GNodeExtensionSet {
			kotlinType = type.kotlinType.classifier
			raptorType = type
		}
	)


	private fun buildScalarDefinition(type: ScalarGraphType) = GCustomScalarType(
		description = type.description,
		name = type.name,
		extensions = GNodeExtensionSet {
			nodeInputCoercer = ScalarCoercer
			outputCoercer = ScalarCoercer
			raptorType = type
			variableInputCoercer = ScalarCoercer
		}
	)


	private fun buildTypeDefinition(type: NamedGraphType) = when (type) {
		is EnumGraphType -> buildEnumDefinition(type)
		is InputObjectGraphType -> buildInputObjectDefinition(type)
		is InterfaceGraphType -> buildInterfaceDefinition(type)
		is ObjectGraphType -> buildObjectDefinition(type)
		is ScalarGraphType -> buildScalarDefinition(type)
		is UnionGraphType -> buildUnionDefinition(type)
	}


	private fun buildTypeDefinitions(): List<GNamedType> =
		typeSystem.types.filterIsInstance<NamedGraphType>().map(::buildTypeDefinition).sortedBy { it.name }


	private fun buildUnionDefinition(type: UnionGraphType) = GUnionType(
		description = type.description,
		name = type.name,
		possibleTypes = resolvePossibleTypesForKotlinType(type.kotlinType),
		extensions = GNodeExtensionSet {
			raptorType = type
		}
	)


	private fun findReferencedDirectiveNames(): Set<String> =
		typeSystem.types.flatMapTo(hashSetOf()) { type ->
			when (type) {
				is AliasGraphType,
				is EnumGraphType,
				is ScalarGraphType,
				is UnionGraphType,
				->
					emptyList()

				is InputObjectGraphType ->
					type.arguments.flatMap { it.directives }.map { it.name }

				is InterfaceGraphType ->
					type.fields.flatMap { it.arguments }.flatMap { it.directives }.map { it.name }

				is ObjectGraphType ->
					type.fields.flatMap { it.arguments }.flatMap { it.directives }.map { it.name }
			}
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

			Maybe::class ->
				return typeRef(checkNotNull(nonNullKotlinType.typeArguments.single()), isInput = isInput)

			else -> when (isInput) {
				true -> typeSystem.resolveInputType(nonNullKotlinType)
				false -> typeSystem.resolveOutputType(nonNullKotlinType)
			}
				.ifNull { error("Cannot resolve GraphQL type for Kotlin type '$nonNullKotlinType'.") } // TODO print stacktrace of usage(s) here
				.let { type ->
					when (type) {
						is AliasGraphType -> when {
							type.isId -> GIdTypeRef
							else -> typeRef(type.referencedKotlinType, isInput = isInput).nullableRef
						}

						is NamedGraphType ->
							GNamedTypeRef(type.name)
					}
				}
		}.let { typeRef ->
			when (kotlinType.isNullable) {
				true -> typeRef
				false -> GNonNullTypeRef(typeRef)
			}
		}
	}


	private fun underlyingType(kotlinType: KotlinType, isInput: Boolean): GraphType {
		@Suppress("NAME_SHADOWING")
		val kotlinType = kotlinType.withNullable(false)

		return when (kotlinType.classifier) {
			Collection::class, List::class, Maybe::class, Set::class -> // TODO improve
				underlyingType(checkNotNull(kotlinType.typeArguments.single()), isInput = isInput)

			else -> when (isInput) {
				true -> typeSystem.resolveInputType(kotlinType)
				false -> typeSystem.resolveOutputType(kotlinType)
			} ?: error("Cannot resolve GraphQL type for Kotlin type '$kotlinType'.")
		}
	}


	companion object {

		fun build(tags: Set<Any>, typeSystem: GraphTypeSystem): DefaultRaptorGraph =
			GraphSystemBuilder(tags = tags, typeSystem = typeSystem).build()
	}
}
