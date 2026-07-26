package io.fluidsonic.raptor.graph


internal class AliasGraphType(
	val convertReferencedToAlias: RaptorGraphInputScope.(input: Any) -> Any,
	val convertAliasToReferenced: RaptorGraphOutputScope.(output: Any) -> Any,
	val isId: Boolean,
	override val isInput: Boolean,
	override val isOutput: Boolean,
	kotlinType: KotlinType,
	val referencedKotlinType: KotlinType,
) : GraphType(
	kotlinType = kotlinType
) {

	init {
		require(isInput || isOutput)
	}
}


internal class EnumGraphType(
	description: String?,
	override val isInput: Boolean,
	override val isOutput: Boolean,
	kotlinType: KotlinType,
	name: String,
	val parse: RaptorGraphInputScope.(input: String) -> Any,
	val serialize: RaptorGraphOutputScope.(output: Any) -> String,
	val values: Collection<EnumValue>,
) : NamedGraphType(
	description = description,
	kotlinType = kotlinType,
	name = name
) {

	init {
		require(isInput || isOutput)
	}
}


internal class EnumValue(
	val description: String?,
	val name: String,
)


internal sealed class GraphType(
	val kotlinType: KotlinType,
) {

	abstract val isInput: Boolean
	abstract val isOutput: Boolean
}


internal class InputObjectGraphType(
	val argumentResolver: ArgumentResolver,
	val arguments: List<GraphArgument>,
	val create: RaptorGraphInputScope.() -> Any,
	description: String?,
	kotlinType: KotlinType,
	name: String,
) : NamedGraphType(
	description = description,
	kotlinType = kotlinType,
	name = name
) {

	override val isInput: Boolean
		get() = true


	override val isOutput: Boolean
		get() = false
}


internal class InterfaceGraphType(
	description: String?,
	val fields: List<GraphField>,
	kotlinType: KotlinType,
	name: String,
) : NamedGraphType(
	description = description,
	kotlinType = kotlinType,
	name = name
) {

	override val isInput: Boolean
		get() = false


	override val isOutput: Boolean
		get() = true
}


internal sealed class NamedGraphType(
	val description: String?,
	kotlinType: KotlinType,
	val name: String,
) : GraphType(
	kotlinType = kotlinType
)


internal class ObjectGraphType(
	description: String?,
	val fields: List<GraphField>,
	kotlinType: KotlinType,
	name: String,
) : NamedGraphType(
	description = description,
	kotlinType = kotlinType,
	name = name
) {

	override val isInput: Boolean
		get() = false


	override val isOutput: Boolean
		get() = true
}


/**
 * A GraphQL scalar type.
 *
 * [parse] and [serialize] are `null` together for the Kotlin-type mappings of GraphQL's built-in scalars, which
 * fluid GraphQL declares and coerces itself. See [ScalarGraphType.hasCoercer].
 */
internal class ScalarGraphType(
	description: String?,
	override val isInput: Boolean,
	override val isOutput: Boolean,
	kotlinType: KotlinType,
	name: String,
	val parse: (RaptorGraphInputScope.(input: Any) -> Any)?,
	val serialize: (RaptorGraphOutputScope.(output: Any) -> Any)?,
) : NamedGraphType(
	description = description,
	kotlinType = kotlinType,
	name = name
) {

	init {
		require(isInput || isOutput)
		require((parse === null) == (serialize === null)) { "'parse' and 'serialize' must either both be defined or both be absent." }
	}


	/** Whether this type brings its own coercion, as opposed to leaving coercion to fluid GraphQL. */
	val hasCoercer: Boolean
		get() = parse !== null
}


internal class UnionGraphType(
	description: String?,
	kotlinType: KotlinType,
	name: String,
) : NamedGraphType(
	description = description,
	kotlinType = kotlinType,
	name = name
) {

	override val isInput: Boolean
		get() = false


	override val isOutput: Boolean
		get() = true
}
