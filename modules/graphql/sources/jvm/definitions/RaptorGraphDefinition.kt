package io.fluidsonic.raptor

import kotlin.reflect.*


// FIXME Raptor prefixes
sealed class RaptorGraphDefinition(
	internal val additionalDefinitions: List<RaptorGraphDefinition>,
	internal val stackTrace: List<StackTraceElement>
) {

	protected fun toString(details: String) =
		"$details\n" + stackTrace.joinToString(separator = "\n") { "\tat $it" }


	companion object {

		var defaultName = "<default>"


		internal fun resolveName(
			name: String,
			defaultNamePrefix: String? = null,
			valueClass: KClass<*>
		) =
			resolveName(name) { defaultNamePrefix.orEmpty() + valueClass.defaultGraphName() }


		internal inline fun resolveName(
			name: String,
			defaultName: () -> String
		) = when (name) {
			RaptorGraphDefinition.defaultName -> defaultName()
			else -> name
		}
	}
}


sealed class GraphNamedTypeDefinition<Value : Any>(
	additionalDefinitions: List<RaptorGraphDefinition>,
	internal val name: String,
	stackTrace: List<StackTraceElement>,
	valueClass: KClass<Value>
) : GraphTypeDefinition<Value>(
	additionalDefinitions = additionalDefinitions,
	stackTrace = stackTrace,
	valueClass = valueClass
) {

	internal abstract val isInput: Boolean
	internal abstract val isOutput: Boolean


	companion object
}


sealed class GraphTypeDefinition<Value : Any>(
	additionalDefinitions: List<RaptorGraphDefinition>,
	stackTrace: List<StackTraceElement>,
	internal val valueClass: KClass<Value>
) : RaptorGraphDefinition(
	additionalDefinitions = additionalDefinitions,
	stackTrace = stackTrace
)


class GraphAliasDefinition<Value : Any, ReferencedValue : Any> internal constructor(
	internal val isId: Boolean,
	internal val parse: RaptorGraphScope.(input: ReferencedValue) -> Value,
	internal val referencedValueClass: KClass<ReferencedValue>,
	internal val serialize: RaptorGraphScope.(value: Value) -> ReferencedValue,
	stackTrace: List<StackTraceElement>,
	valueClass: KClass<Value>
) : GraphTypeDefinition<Value>(
	additionalDefinitions = emptyList(),
	stackTrace = stackTrace,
	valueClass = valueClass
) {

	override fun toString() =
		toString("${if (isId) "id" else "alias"} ${valueClass.qualifiedName} -> ${referencedValueClass.qualifiedName}")
}


class GraphEnumDefinition<Value : Enum<Value>> internal constructor(
	internal val description: String?,
	name: String,
	stackTrace: List<StackTraceElement>,
	valueClass: KClass<Value>,
	internal val values: List<Value>
) : GraphNamedTypeDefinition<Value>(
	additionalDefinitions = emptyList(),
	name = name,
	stackTrace = stackTrace,
	valueClass = valueClass
) {

	override val isInput: Boolean
		get() = true


	override val isOutput: Boolean
		get() = true


	override fun toString() =
		toString("enum '$name' ${valueClass.qualifiedName}")


	private enum class Dummy
}


internal class GraphFieldDefinition<in Parent : Any, out Value>(
	val arguments: List<GraphArgumentDefinition<*>>,
	val description: String?,
	val name: String,
	val resolver: (suspend RaptorGraphScope.(parent: Parent) -> Value)?,
	val valueType: KType
)


class GraphInputObjectDefinition<Value : Any> internal constructor(
	internal val arguments: List<GraphArgumentDefinition<*>>,
	internal val description: String?,
	internal val factory: RaptorGraphScope.() -> Value,
	name: String,
	nestedDefinitions: List<GraphNamedTypeDefinition<*>>,
	stackTrace: List<StackTraceElement>,
	valueClass: KClass<Value>
) : GraphNamedTypeDefinition<Value>(
	additionalDefinitions = nestedDefinitions,
	name = name,
	stackTrace = stackTrace,
	valueClass = valueClass
) {

	override val isInput: Boolean
		get() = true


	override val isOutput: Boolean
		get() = false


	override fun toString() =
		toString("input '$name' ${valueClass.qualifiedName}")
}


class GraphInterfaceDefinition<Value : Any> internal constructor(
	internal val description: String?,
	internal val fields: List<GraphFieldDefinition<Value, *>>,
	name: String,
	nestedDefinitions: List<GraphNamedTypeDefinition<*>>,
	stackTrace: List<StackTraceElement>,
	valueClass: KClass<Value>
) : GraphNamedTypeDefinition<Value>(
	additionalDefinitions = nestedDefinitions,
	name = name,
	stackTrace = stackTrace,
	valueClass = valueClass
) {

	override val isInput: Boolean
		get() = false


	override val isOutput: Boolean
		get() = true


	override fun toString() =
		toString("interface '$name' ${valueClass.qualifiedName}")
}


class GraphInterfaceExtensionDefinition<Value : Any> internal constructor(
	internal val fields: List<GraphFieldDefinition<Value, *>>, // FIXME use!
	stackTrace: List<StackTraceElement>,
	internal val valueClass: KClass<Value>
) : RaptorGraphDefinition(
	additionalDefinitions = emptyList(),
	stackTrace = stackTrace
) {

	override fun toString() =
		toString("extension for interface of Kotlin type ${valueClass.qualifiedName}")
}


class GraphObjectDefinition<Value : Any> internal constructor(
	internal val description: String?,
	internal val fields: List<GraphFieldDefinition<Value, *>>,
	name: String,
	nestedDefinitions: List<GraphNamedTypeDefinition<*>>,
	stackTrace: List<StackTraceElement>,
	valueClass: KClass<Value>
) : GraphNamedTypeDefinition<Value>(
	additionalDefinitions = nestedDefinitions,
	name = name,
	stackTrace = stackTrace,
	valueClass = valueClass
) {

	override val isInput: Boolean
		get() = false


	override val isOutput: Boolean
		get() = true


	override fun toString() =
		toString("type '$name' ${valueClass.qualifiedName}")
}


class GraphObjectExtensionDefinition<Value : Any> internal constructor(
	internal val fields: List<GraphFieldDefinition<Value, *>>,
	stackTrace: List<StackTraceElement>,
	internal val valueClass: KClass<Value>
) : RaptorGraphDefinition(
	additionalDefinitions = emptyList(),
	stackTrace = stackTrace
) {

	override fun toString() =
		toString("extension for object of Kotlin type ${valueClass.qualifiedName}")
}


class GraphOperationDefinition<Value> internal constructor(
	additionalDefinitions: List<GraphNamedTypeDefinition<*>>,
	internal val field: GraphFieldDefinition<Unit, Value>,
	stackTrace: List<StackTraceElement>,
	internal val type: RaptorGraphOperationType
) : RaptorGraphDefinition(
	additionalDefinitions = additionalDefinitions,
	stackTrace = stackTrace
) {

	override fun toString() =
		toString("$type '${field.name}' -> ${field.valueType}")
}


class GraphScalarDefinition<Value : Any> internal constructor(
	internal val description: String?,
	internal val jsonInputClass: KClass<*>,
	name: String,
	internal val parseBoolean: (RaptorGraphArgumentParsingScope.(input: Boolean) -> Value)?,
	internal val parseFloat: (RaptorGraphArgumentParsingScope.(input: Double) -> Value)?,
	internal val parseInt: (RaptorGraphArgumentParsingScope.(input: Int) -> Value)?,
	internal val parseJson: RaptorGraphArgumentParsingScope.(input: Any) -> Value,
	internal val parseObject: (RaptorGraphArgumentParsingScope.(input: Map<String, *>) -> Value)?,
	internal val parseString: (RaptorGraphArgumentParsingScope.(input: String) -> Value)?,
	internal val serializeJson: RaptorGraphScope.(value: Value) -> Any,
	stackTrace: List<StackTraceElement>,
	valueClass: KClass<Value>
) : GraphNamedTypeDefinition<Value>(
	additionalDefinitions = emptyList(),
	name = name,
	stackTrace = stackTrace,
	valueClass = valueClass
) {

	override val isInput: Boolean
		get() = true


	override val isOutput: Boolean
		get() = true


	override fun toString() =
		toString("scalar '$name' ${valueClass.qualifiedName}")
}
