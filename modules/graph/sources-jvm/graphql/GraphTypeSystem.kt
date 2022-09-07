package io.fluidsonic.raptor.graph


internal class GraphTypeSystem(
	val types: Collection<GraphType>,
) {

	private val typesByName = types.filterIsInstance<NamedGraphType>().associateBy { it.name }
	private val inputTypesByValueType = types.filter { it.isInput }.associateBy { it.kotlinType }
	private val outputTypesByValueType = types.filter { it.isOutput }.associateBy { it.kotlinType }


	fun resolveInputType(kotlinType: KotlinType) =
		inputTypesByValueType[kotlinType]


	fun resolveOutputType(kotlinType: KotlinType) =
		outputTypesByValueType[kotlinType]


	fun resolveType(name: String) =
		typesByName[name]
}
