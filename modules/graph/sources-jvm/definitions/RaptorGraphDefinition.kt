package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*
import kotlin.properties.*
import kotlin.reflect.*


public sealed class RaptorGraphNode(
	internal val additionalDefinitions: Collection<RaptorGraphDefinition>,
	internal val stackTrace: List<StackTraceElement>,
) {

	protected abstract fun debugString(): String


	final override fun toString(): String =
		"${debugString()}\n" + stackTrace.joinToString(separator = "\n") { "\tat $it" }
}


public sealed class RaptorGraphDefinition(
	additionalDefinitions: Collection<RaptorGraphDefinition>,
	stackTrace: List<StackTraceElement>,
) : RaptorGraphNode(
	additionalDefinitions = additionalDefinitions,
	stackTrace = stackTrace
) {

	public companion object {

		@PublishedApi
		internal const val defaultName: String = "<default>"


		// TODO move
		internal fun resolveName(
			name: String,
			defaultNamePrefix: String? = null,
			type: KType,
		) =
			resolveName(name) { defaultNamePrefix.orEmpty() + type.defaultGraphName() }


		// TODO move
		internal inline fun resolveName(
			name: String,
			defaultName: () -> String,
		) = when (name) {
			RaptorGraphDefinition.defaultName -> defaultName()
			else -> name
		}
	}
}


internal class AliasGraphTypeDefinition(
	additionalDefinitions: Collection<RaptorGraphDefinition>,
	val convertReferencedToAlias: RaptorGraphInputScope.(input: Any) -> Any,
	val convertAliasToReferenced: RaptorGraphOutputScope.(output: Any) -> Any,
	val isId: Boolean,
	override val isInput: Boolean,
	override val isOutput: Boolean,
	kotlinType: KotlinType,
	val referencedKotlinType: KotlinType,
	stackTrace: List<StackTraceElement>,
) : GraphTypeDefinition(
	additionalDefinitions = additionalDefinitions,
	kotlinType = kotlinType,
	stackTrace = stackTrace
) {

	init {
		require(!kotlinType.isGeneric || !isId)
		require(isInput || isOutput)
	}


	override fun debugString() =
		"${if (isId) "id" else "alias"} $kotlinType = $referencedKotlinType"


	override fun specialize(typeArguments: List<KotlinType>, namePrefix: String): AliasGraphTypeDefinition {
		check(kotlinType.isGeneric)

		return AliasGraphTypeDefinition(
			additionalDefinitions = additionalDefinitions,
			convertAliasToReferenced = convertAliasToReferenced,
			convertReferencedToAlias = convertReferencedToAlias,
			isId = isId,
			isInput = isInput,
			isOutput = isOutput,
			kotlinType = kotlinType.specialize(typeArguments),
			referencedKotlinType = referencedKotlinType,
			stackTrace = stackTrace
		)
	}
}


internal class EnumGraphDefinition(
	additionalDefinitions: Collection<RaptorGraphDefinition>,
	description: String?,
	override val isInput: Boolean,
	override val isOutput: Boolean,
	kotlinType: KotlinType,
	name: String,
	val parse: RaptorGraphInputScope.(input: String) -> Any,
	val serialize: RaptorGraphOutputScope.(output: Any) -> String,
	stackTrace: List<StackTraceElement>,
	val values: Set<String>,
) : NamedGraphTypeDefinition(
	additionalDefinitions = additionalDefinitions,
	description = description,
	kotlinType = kotlinType,
	name = name,
	stackTrace = stackTrace,
) {

	init {
		require(!kotlinType.isGeneric)
		require(isInput || isOutput)
	}


	override fun debugString() =
		"enum $name = $kotlinType"


	override fun specialize(typeArguments: List<KotlinType>, namePrefix: String): EnumGraphDefinition {
		error("Enum definitions cannot be generic.")
	}
}


internal class GraphArgumentDefinition(
	val defaultValue: GValue?,
	val description: String?,
	val kotlinType: KotlinType,
	name: String?,
	val resolver: ArgumentResolver,
	stackTrace: List<StackTraceElement>,
) : RaptorGraphNode(
	additionalDefinitions = emptyList(),
	stackTrace = stackTrace
),
	RaptorGraphArgumentDelegate<Any?> {

	private var isProvided = false
	private var transforms = emptyList<RaptorGraphInputScope.(Any?) -> Any?>()

	internal var name = name // TODO validate uniqueness
		private set


	override fun debugString() =
		"argument '$name': $kotlinType"


	override fun <TransformedType> map(transform: RaptorGraphInputScope.(value: Any?) -> TransformedType): RaptorGraphArgumentDelegate<TransformedType> {
		// TODO freeze after builder is done

		transforms = transforms + transform

		@Suppress("UNCHECKED_CAST")
		return this as RaptorGraphArgumentDelegate<TransformedType>
	}


	override operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadOnlyProperty<Any?, Any?> {
		check(!isProvided) { "Cannot delegate multiple variables to the same argument." }

		val variableName = property.name
		val name = name ?: variableName

		this.isProvided = true
		this.name = name

		return ReadOnlyProperty { _, _ ->
			resolveArgument(name = name, variableName = variableName)
		}
	}


	private fun resolveArgument(name: String, variableName: String): Any? =
		resolver.resolveArgument(name = name, variableName = variableName, transforms = transforms)


	fun specialize(typeArguments: List<KotlinType>): GraphArgumentDefinition {
		if (kotlinType.isSpecialized)
			return this

		return GraphArgumentDefinition(
			defaultValue = defaultValue,
			description = description,
			kotlinType = kotlinType.specialize(typeArguments),
			name = name,
			resolver = resolver,
			stackTrace = stackTrace
		)
	}
}


internal sealed class GraphFieldDefinition(
	val argumentDefinitions: Collection<GraphArgumentDefinition>,
	val description: String?,
	val kotlinType: KotlinType,
	val name: String,
	stackTrace: List<StackTraceElement>,
) : RaptorGraphNode(
	additionalDefinitions = emptyList(),
	stackTrace = stackTrace
) {

	override fun debugString() =
		"field '$name': $kotlinType"


	abstract fun specialize(typeArguments: List<KotlinType>): GraphFieldDefinition


	class Resolvable(
		argumentDefinitions: Collection<GraphArgumentDefinition>,
		val argumentResolver: ArgumentResolver,
		description: String?,
		kotlinType: KotlinType,
		name: String,
		val resolve: suspend RaptorGraphOutputScope.(parent: Any) -> Any?,
		stackTrace: List<StackTraceElement>,
	) : GraphFieldDefinition(
		argumentDefinitions = argumentDefinitions,
		description = description,
		kotlinType = kotlinType,
		name = name,
		stackTrace = stackTrace
	) {

		override fun specialize(typeArguments: List<KotlinType>) = Resolvable(
			argumentDefinitions = argumentDefinitions.map { it.specialize(typeArguments) },
			argumentResolver = argumentResolver,
			description = description,
			kotlinType = kotlinType.specialize(typeArguments),
			name = name,
			resolve = resolve,
			stackTrace = stackTrace
		)
	}


	class Unresolvable(
		argumentDefinitions: Collection<GraphArgumentDefinition>,
		description: String?,
		kotlinType: KotlinType,
		name: String,
		stackTrace: List<StackTraceElement>,
	) : GraphFieldDefinition(
		argumentDefinitions = argumentDefinitions,
		description = description,
		kotlinType = kotlinType,
		name = name,
		stackTrace = stackTrace
	) {

		override fun specialize(typeArguments: List<KotlinType>) = Unresolvable(
			argumentDefinitions = argumentDefinitions.map { it.specialize(typeArguments) },
			description = description,
			kotlinType = kotlinType.specialize(typeArguments),
			name = name,
			stackTrace = stackTrace
		)
	}
}


internal class GraphOperationDefinition(
	additionalDefinitions: Collection<RaptorGraphDefinition>,
	val fieldDefinition: GraphFieldDefinition,
	val operationType: RaptorGraphOperationType,
	stackTrace: List<StackTraceElement>,
) : RaptorGraphDefinition(
	additionalDefinitions = additionalDefinitions,
	stackTrace = stackTrace
) {

	override fun debugString() =
		"$operationType '${fieldDefinition.name}': ${fieldDefinition.kotlinType}"
}


internal sealed class GraphTypeDefinition(
	additionalDefinitions: Collection<RaptorGraphDefinition>,
	kotlinType: KotlinType,
	stackTrace: List<StackTraceElement>,
) : GraphTypeSystemDefinition(
	additionalDefinitions = additionalDefinitions,
	stackTrace = stackTrace,
	kotlinType = kotlinType
) {

	abstract val isInput: Boolean
	abstract val isOutput: Boolean
}


internal sealed class GraphTypeExtensionDefinition(
	additionalDefinitions: Collection<RaptorGraphDefinition>,
	kotlinType: KotlinType,
	stackTrace: List<StackTraceElement>,
) : GraphTypeSystemDefinition(
	additionalDefinitions = additionalDefinitions,
	kotlinType = kotlinType,
	stackTrace = stackTrace
)


internal sealed class GraphTypeSystemDefinition(
	additionalDefinitions: Collection<RaptorGraphDefinition>,
	val kotlinType: KotlinType,
	stackTrace: List<StackTraceElement>,
) : RaptorGraphDefinition(
	additionalDefinitions = additionalDefinitions,
	stackTrace = stackTrace
) {

	abstract fun specialize(typeArguments: List<KotlinType>, namePrefix: String): GraphTypeSystemDefinition
}


internal class InputObjectGraphDefinition(
	additionalDefinitions: Collection<RaptorGraphDefinition>,
	val argumentDefinitions: Collection<GraphArgumentDefinition>,
	val argumentResolver: ArgumentResolver,
	val create: RaptorGraphInputScope.() -> Any,
	description: String?,
	kotlinType: KotlinType,
	name: String,
	stackTrace: List<StackTraceElement>,
) : NamedGraphTypeDefinition(
	additionalDefinitions = additionalDefinitions,
	description = description,
	kotlinType = kotlinType,
	name = name,
	stackTrace = stackTrace
) {

	override fun debugString() =
		"input $name = $kotlinType"


	override val isInput: Boolean
		get() = true


	override val isOutput: Boolean
		get() = false


	override fun specialize(typeArguments: List<KotlinType>, namePrefix: String): InputObjectGraphDefinition {
		check(kotlinType.isGeneric)

		return InputObjectGraphDefinition(
			additionalDefinitions = additionalDefinitions,
			argumentDefinitions = argumentDefinitions.map { it.specialize(typeArguments) },
			argumentResolver = argumentResolver,
			create = create,
			description = description,
			kotlinType = kotlinType.specialize(typeArguments),
			name = "${namePrefix}$name",
			stackTrace = stackTrace
		)
	}
}


internal class InterfaceGraphDefinition(
	additionalDefinitions: Collection<RaptorGraphDefinition>,
	description: String?,
	val fieldDefinitions: Collection<GraphFieldDefinition>,
	kotlinType: KotlinType,
	name: String,
	stackTrace: List<StackTraceElement>,
) : NamedGraphTypeDefinition(
	additionalDefinitions = additionalDefinitions,
	description = description,
	kotlinType = kotlinType,
	name = name,
	stackTrace = stackTrace
) {

	override fun debugString() =
		"interface $name: $kotlinType"


	override val isInput: Boolean
		get() = false


	override val isOutput: Boolean
		get() = true


	override fun specialize(typeArguments: List<KotlinType>, namePrefix: String): InterfaceGraphDefinition {
		check(kotlinType.isGeneric)

		return InterfaceGraphDefinition(
			additionalDefinitions = additionalDefinitions,
			description = description,
			fieldDefinitions = fieldDefinitions.map { it.specialize(typeArguments) },
			kotlinType = kotlinType.specialize(typeArguments),
			name = "${namePrefix}$name",
			stackTrace = stackTrace
		)
	}
}


internal sealed class NamedGraphTypeDefinition(
	additionalDefinitions: Collection<RaptorGraphDefinition>,
	val description: String?,
	kotlinType: KotlinType,
	val name: String,
	stackTrace: List<StackTraceElement>,
) : GraphTypeDefinition(
	additionalDefinitions = additionalDefinitions,
	kotlinType = kotlinType,
	stackTrace = stackTrace
)


internal class InterfaceExtensionGraphDefinition(
	additionalDefinitions: Collection<RaptorGraphDefinition>,
	val fieldDefinitions: Collection<GraphFieldDefinition>,
	kotlinType: KotlinType,
	stackTrace: List<StackTraceElement>,
) : GraphTypeExtensionDefinition(
	additionalDefinitions = additionalDefinitions,
	kotlinType = kotlinType,
	stackTrace = stackTrace
) {

	override fun debugString() =
		"extension interface: $kotlinType"


	override fun specialize(typeArguments: List<KotlinType>, namePrefix: String): InterfaceExtensionGraphDefinition {
		check(kotlinType.isGeneric)

		return InterfaceExtensionGraphDefinition(
			additionalDefinitions = additionalDefinitions,
			fieldDefinitions = fieldDefinitions.map { it.specialize(typeArguments) },
			kotlinType = kotlinType.specialize(typeArguments),
			stackTrace = stackTrace
		)
	}
}


internal class ObjectGraphDefinition(
	additionalDefinitions: Collection<RaptorGraphDefinition>,
	description: String?,
	val fieldDefinitions: Collection<GraphFieldDefinition>,
	kotlinType: KotlinType,
	name: String,
	stackTrace: List<StackTraceElement>,
) : NamedGraphTypeDefinition(
	additionalDefinitions = additionalDefinitions,
	description = description,
	kotlinType = kotlinType,
	name = name,
	stackTrace = stackTrace
) {

	override fun debugString() =
		"type $name: $kotlinType"


	override val isInput: Boolean
		get() = false


	override val isOutput: Boolean
		get() = true


	override fun specialize(typeArguments: List<KotlinType>, namePrefix: String): ObjectGraphDefinition {
		check(kotlinType.isGeneric)

		return ObjectGraphDefinition(
			additionalDefinitions = additionalDefinitions,
			description = description,
			fieldDefinitions = fieldDefinitions.map { it.specialize(typeArguments) },
			kotlinType = kotlinType.specialize(typeArguments),
			name = "${namePrefix}$name",
			stackTrace = stackTrace
		)
	}
}


internal class ObjectExtensionGraphDefinition(
	additionalDefinitions: Collection<RaptorGraphDefinition>,
	val fieldDefinitions: Collection<GraphFieldDefinition>,
	kotlinType: KotlinType,
	stackTrace: List<StackTraceElement>,
) : GraphTypeExtensionDefinition(
	additionalDefinitions = additionalDefinitions,
	kotlinType = kotlinType,
	stackTrace = stackTrace
) {

	override fun debugString() =
		"extension object: $kotlinType"


	override fun specialize(typeArguments: List<KotlinType>, namePrefix: String): ObjectExtensionGraphDefinition {
		check(kotlinType.isGeneric)

		return ObjectExtensionGraphDefinition(
			additionalDefinitions = additionalDefinitions,
			fieldDefinitions = fieldDefinitions.map { it.specialize(typeArguments) },
			kotlinType = kotlinType.specialize(typeArguments),
			stackTrace = stackTrace
		)
	}
}


internal class ScalarGraphDefinition(
	additionalDefinitions: Collection<RaptorGraphDefinition>,
	description: String?,
	override val isInput: Boolean,
	override val isOutput: Boolean,
	kotlinType: KotlinType,
	name: String,
	val parse: RaptorGraphInputScope.(input: Any) -> Any,
	val serialize: RaptorGraphOutputScope.(output: Any) -> Any,
	stackTrace: List<StackTraceElement>,
) : NamedGraphTypeDefinition(
	additionalDefinitions = additionalDefinitions,
	description = description,
	kotlinType = kotlinType,
	name = name,
	stackTrace = stackTrace
) {

	init {
		require(isInput || isOutput)
	}


	override fun debugString() =
		"scalar $name: $kotlinType"


	override fun specialize(typeArguments: List<KotlinType>, namePrefix: String): ScalarGraphDefinition {
		check(kotlinType.isGeneric)

		return ScalarGraphDefinition(
			additionalDefinitions = additionalDefinitions,
			description = description,
			isInput = isInput,
			isOutput = isOutput,
			kotlinType = kotlinType.specialize(typeArguments),
			name = "${namePrefix}$name",
			parse = parse,
			serialize = serialize,
			stackTrace = stackTrace
		)
	}
}


internal class UnionGraphDefinition(
	additionalDefinitions: Collection<RaptorGraphDefinition>,
	description: String?,
	kotlinType: KotlinType,
	name: String,
	stackTrace: List<StackTraceElement>,
) : NamedGraphTypeDefinition(
	additionalDefinitions = additionalDefinitions,
	description = description,
	kotlinType = kotlinType,
	name = name,
	stackTrace = stackTrace
) {

	override fun debugString() =
		"union $name: $kotlinType"


	override val isInput: Boolean
		get() = false


	override val isOutput: Boolean
		get() = true


	override fun specialize(typeArguments: List<KotlinType>, namePrefix: String): UnionGraphDefinition {
		check(kotlinType.isGeneric)

		return UnionGraphDefinition(
			additionalDefinitions = additionalDefinitions,
			description = description,
			kotlinType = kotlinType.specialize(typeArguments),
			name = "${namePrefix}$name",
			stackTrace = stackTrace
		)
	}
}
