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
	val values: Set<String>,
) : NamedGraphType(
	description = description,
	kotlinType = kotlinType,
	name = name
) {

	init {
		require(isInput || isOutput)
	}
}


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


internal class ScalarGraphType(
	description: String?,
	override val isInput: Boolean,
	override val isOutput: Boolean,
	kotlinType: KotlinType,
	name: String,
	val parse: RaptorGraphInputScope.(input: Any) -> Any,
	val serialize: RaptorGraphOutputScope.(output: Any) -> Any,
) : NamedGraphType(
	description = description,
	kotlinType = kotlinType,
	name = name
) {

	init {
		require(isInput || isOutput)
	}
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
