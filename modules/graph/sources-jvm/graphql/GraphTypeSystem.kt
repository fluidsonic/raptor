package io.fluidsonic.raptor.graph


/**
 * The GraphQL types a graph is made of, resolvable by the Kotlin type they represent.
 *
 * [types] holds exactly the types raptor emits into the schema document. A Kotlin type that maps onto a type someone
 * else declares — GraphQL's built-in scalars, which fluid GraphQL declares and coerces — has no entry there and
 * resolves through [resolveExternallyDeclaredTypeName] instead, by name only.
 */
internal class GraphTypeSystem(
	private val externallyDeclaredTypeNamesByKotlinType: Map<KotlinType, String>,
	val types: Collection<GraphType>,
) {

	private val inputTypesByValueType = types.filter { it.isInput }.associateBy { it.kotlinType }
	private val outputTypesByValueType = types.filter { it.isOutput }.associateBy { it.kotlinType }


	/** The name of the GraphQL type [kotlinType] maps onto if that type is declared outside raptor, else `null`. */
	fun resolveExternallyDeclaredTypeName(kotlinType: KotlinType) =
		externallyDeclaredTypeNamesByKotlinType[kotlinType]


	fun resolveInputType(kotlinType: KotlinType) =
		inputTypesByValueType[kotlinType]


	fun resolveOutputType(kotlinType: KotlinType) =
		outputTypesByValueType[kotlinType]
}
