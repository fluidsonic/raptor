package io.fluidsonic.raptor.graph

import io.fluidsonic.stdlib.*
import kotlin.reflect.*


// TODO remove unreferenced definitions
internal class GraphTypeSystemBuilder private constructor(
	systemDefinition: GraphSystemDefinition,
) {

	private val interfaceExtensionDefinitionsByKotlinType = systemDefinition.definitions
		.filterIsInstance<InterfaceExtensionGraphDefinition>()
		.groupBy { it.kotlinType.specialize() }
		.mapValues { (_, definitions) -> definitions.flatMap { it.fieldDefinitions } }

	private val objectExtensionDefinitionsByKotlinType = systemDefinition.definitions
		.filterIsInstance<ObjectExtensionGraphDefinition>()
		.groupBy { it.kotlinType.specialize() }
		.mapValues { (_, definitions) -> definitions.flatMap { it.fieldDefinitions } }

	private val operationDefinitionsByType = systemDefinition.definitions
		.filterIsInstance<GraphOperationDefinition>()
		.groupBy { it.operationType }

	private val typeDefinitions = systemDefinition.definitions.filterIsInstance<GraphTypeDefinition>()


	private fun build() =
		GraphTypeSystem(
			types = typeDefinitions.map(::buildType) +
				operationDefinitionsByType.map { (operationType, definitions) ->
					buildType(operationType, definitions)
				}
		)


	private fun buildType(operationType: RaptorGraphOperationType, definitions: Collection<GraphOperationDefinition>) =
		ObjectGraphType(
			description = null,
			kotlinType = KotlinType.of(
				type = when (operationType) {
					RaptorGraphOperationType.mutation -> typeOf<MutationRoot>()
					RaptorGraphOperationType.query -> typeOf<QueryRoot>()
				},
				containingType = null,
				allowMaybe = false,
				allowNull = false,
				allowedVariance = KVariance.INVARIANT,
				requireSpecialization = false
			),
			fields = definitions.map { buildField(it.fieldDefinition) },
			name = operationType.gqlType.defaultObjectTypeName
		)


	private fun buildType(definition: GraphTypeDefinition): GraphType {
		check(definition.kotlinType.isSpecialized) { "Definition has not been specialized:\n$definition\n---" }

		return when (definition) {
			is AliasGraphTypeDefinition ->
				buildAliasType(definition)

			is EnumGraphDefinition ->
				buildEnumType(definition)

			is InputObjectGraphDefinition ->
				buildInputObjectType(definition)

			is InterfaceGraphDefinition ->
				buildInterfaceType(definition)

			is ObjectGraphDefinition ->
				buildObjectType(definition)

			is ScalarGraphDefinition ->
				buildScalarType(definition)

			is UnionGraphDefinition ->
				buildUnionType(definition)
		}
	}


	private fun buildAliasType(definition: AliasGraphTypeDefinition) =
		AliasGraphType(
			convertAliasToReferenced = definition.convertAliasToReferenced,
			convertReferencedToAlias = definition.convertReferencedToAlias,
			isId = definition.isId,
			isInput = definition.isInput,
			isOutput = definition.isOutput,
			kotlinType = definition.kotlinType,
			referencedKotlinType = definition.referencedKotlinType
		)


	private fun buildEnumType(definition: EnumGraphDefinition) =
		EnumGraphType(
			description = definition.description,
			isInput = definition.isInput,
			isOutput = definition.isOutput,
			kotlinType = definition.kotlinType,
			name = definition.name,
			parse = definition.parse,
			serialize = definition.serialize,
			values = definition.values
		)


	private fun buildField(definition: GraphFieldDefinition) = when (definition) {
		is GraphFieldDefinition.Resolvable -> GraphField.Resolvable(
			argumentResolver = definition.argumentResolver,
			arguments = definition.argumentDefinitions.map { argumentDefinition ->
				GraphArgument(
					defaultValue = argumentDefinition.defaultValue,
					description = argumentDefinition.description,
					directives = directivesForArgument(argumentDefinition),
					kotlinType = argumentDefinition.kotlinType.specialize(),
					name = argumentDefinition.name!! // TODO Hack.
				)
			},
			description = definition.description,
			kotlinType = definition.kotlinType.specialize(),
			name = definition.name,
			resolve = definition.resolve
		)

		is GraphFieldDefinition.Unresolvable -> GraphField.Unresolvable(
			arguments = definition.argumentDefinitions.map { argumentDefinition ->
				GraphArgument(
					defaultValue = argumentDefinition.defaultValue,
					description = argumentDefinition.description,
					directives = directivesForArgument(argumentDefinition),
					kotlinType = argumentDefinition.kotlinType.specialize(),
					name = argumentDefinition.name!! // TODO Hack.
				)
			},
			description = definition.description,
			kotlinType = definition.kotlinType.specialize(),
			name = definition.name,
		)
	}


	private fun buildInputObjectType(definition: InputObjectGraphDefinition) =
		InputObjectGraphType(
			argumentResolver = definition.argumentResolver,
			arguments = definition.argumentDefinitions.map { argumentDefinition ->
				GraphArgument(
					defaultValue = argumentDefinition.defaultValue,
					description = argumentDefinition.description,
					directives = directivesForArgument(argumentDefinition),
					kotlinType = argumentDefinition.kotlinType.specialize(),
					name = argumentDefinition.name!! // TODO Hack.
				)
			},
			create = definition.create,
			description = definition.description,
			kotlinType = definition.kotlinType,
			name = definition.name
		)


	private fun buildInterfaceType(definition: InterfaceGraphDefinition) =
		InterfaceGraphType(
			description = definition.description,
			fields = (definition.fieldDefinitions + interfaceExtensionDefinitionsByKotlinType[definition.kotlinType.specialize()].orEmpty())
				.map(::buildField),
			kotlinType = definition.kotlinType,
			name = definition.name
		)


	private fun buildObjectType(definition: ObjectGraphDefinition) =
		ObjectGraphType(
			description = definition.description,
			fields = (definition.fieldDefinitions + objectExtensionDefinitionsByKotlinType[definition.kotlinType.specialize()].orEmpty())
				.map(::buildField),
			kotlinType = definition.kotlinType,
			name = definition.name
		)


	private fun buildScalarType(definition: ScalarGraphDefinition) =
		ScalarGraphType(
			description = definition.description,
			isInput = definition.isInput,
			isOutput = definition.isOutput,
			kotlinType = definition.kotlinType,
			name = definition.name,
			parse = definition.parse,
			serialize = definition.serialize
		)


	private fun buildUnionType(definition: UnionGraphDefinition) =
		UnionGraphType(
			description = definition.description,
			kotlinType = definition.kotlinType,
			name = definition.name
		)


	private fun directivesForArgument(argument: GraphArgumentDefinition) =
		when (argument.kotlinType.classifier) {
			Maybe::class -> listOf(GraphDirective.optional)
			else -> emptyList()
		}


	companion object {

		fun build(systemDefinition: GraphSystemDefinition): GraphTypeSystem =
			GraphTypeSystemBuilder(systemDefinition = systemDefinition).build()
	}


	private object MutationRoot
	private object QueryRoot
}
