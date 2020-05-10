package io.fluidsonic.raptor

import io.fluidsonic.graphql.*
import io.fluidsonic.stdlib.*
import kotlin.reflect.*
import kotlin.reflect.full.*


// FIXME prevent Any inference (check at runtime)
// FIXME add missing types
// FIXME add DSL markers
// FIXME rename all the "kotlin" stuff
// FIXME use own simple wrapper around KClass with nullability info instead of KType?

fun graphEnumDefinition(configure: GraphEnumDefinition.Builder.() -> Unit) =
	GraphEnumDefinition.Builder(
		stackTrace = stackTrace(skipCount = 1)
	)
		.apply(configure)
		.build()


fun graphInputObjectDefinition(configure: GraphInputObjectDefinition.Builder.() -> Unit) =
	GraphInputObjectDefinition.Builder(
		stackTrace = stackTrace(skipCount = 1)
	)
		.apply(configure)
		.build()


fun graphInterfaceDefinition(configure: GraphInterfaceDefinition.Builder.() -> Unit) =
	GraphInterfaceDefinition.Builder(
		stackTrace = stackTrace(skipCount = 1)
	)
		.apply(configure)
		.build()


inline fun <reified Value : Any> graphInterfaceExtensionDefinition(
	noinline configure: GraphInterfaceExtensionDefinition.Builder<Value>.() -> Unit
) =
	graphInterfaceExtensionDefinition(Value::class, configure = configure)


fun <Value : Any> graphInterfaceExtensionDefinition(
	valueClass: KClass<Value>,
	configure: GraphInterfaceExtensionDefinition.Builder<Value>.() -> Unit
) =
	GraphInterfaceExtensionDefinition.Builder(
		stackTrace = stackTrace(skipCount = 1),
		valueClass = valueClass
	)
		.apply(configure)
		.build()


fun graphMutationDefinition(name: String, configure: GraphOperationDefinition.Builder.() -> Unit) =
	GraphOperationDefinition.Builder(
		name = name,
		type = GraphOperation.Type.mutation,
		stackTrace = stackTrace(skipCount = 1)
	)
		.apply(configure)
		.build()


fun graphObjectDefinition(configure: GraphObjectDefinition.Builder.() -> Unit) =
	GraphObjectDefinition.Builder(
		stackTrace = stackTrace(skipCount = 1)
	)
		.apply(configure)
		.build()


inline fun <reified Value : Any> graphObjectExtensionDefinition(
	noinline configure: GraphObjectExtensionDefinition.Builder<Value>.() -> Unit
) =
	graphObjectExtensionDefinition(Value::class, configure = configure)


fun <Value : Any> graphObjectExtensionDefinition(
	valueClass: KClass<Value>,
	configure: GraphObjectExtensionDefinition.Builder<Value>.() -> Unit
) =
	GraphObjectExtensionDefinition.Builder(
		stackTrace = stackTrace(skipCount = 1),
		valueClass = valueClass
	)
		.apply(configure)
		.build()


fun graphQueryDefinition(name: String, configure: GraphOperationDefinition.Builder.() -> Unit) =
	GraphOperationDefinition.Builder(
		name = name,
		type = GraphOperation.Type.query,
		stackTrace = stackTrace(skipCount = 1)
	)
		.apply(configure)
		.build()


fun graphScalarDefinition(configure: GraphScalarDefinition.Builder.() -> Unit) =
	GraphScalarDefinition.Builder(
		stackTrace = stackTrace(skipCount = 1)
	)
		.apply(configure)
		.build()


fun graphAliasDefinition(configure: GraphAliasDefinition.Builder.() -> Unit) =
	GraphAliasDefinition.Builder(
		stackTrace = stackTrace(skipCount = 1)
	)
		.apply(configure)
		.build()


sealed class RaptorGraphDefinition(
	internal val stackTrace: List<StackTraceElement>
) {

	protected fun toString(details: String) =
		"$details\n" + stackTrace.joinToString(separator = "\n") { "\tat $it" }
}


sealed class GraphNamedTypeDefinition<Value : Any>(
	internal val name: String,
	stackTrace: List<StackTraceElement>,
	valueClass: KClass<Value>
) : GraphTypeDefinition<Value>(
	stackTrace = stackTrace,
	valueClass = valueClass
)


sealed class GraphTypeDefinition<Value : Any>(
	stackTrace: List<StackTraceElement>,
	internal val valueClass: KClass<Value>
) : RaptorGraphDefinition(
	stackTrace = stackTrace
)


class GraphAliasDefinition<Value : Any, ReferencedValue : Any> internal constructor(
	val parse: RaptorGraphScope.(input: ReferencedValue) -> Value?,
	val referencedValueClass: KClass<ReferencedValue>,
	val serialize: RaptorGraphScope.(value: Value) -> ReferencedValue,
	stackTrace: List<StackTraceElement>,
	valueClass: KClass<Value>
) : GraphTypeDefinition<Value>(
	stackTrace = stackTrace,
	valueClass = valueClass
) {

	override fun toString() =
		toString("alias ${valueClass.qualifiedName} -> ${referencedValueClass.qualifiedName}")


	class Builder internal constructor(
		private val stackTrace: List<StackTraceElement>
	) {

		private var parse: (RaptorGraphScope.(input: Any) -> Any?)? = null
		private var referencedValueClass: KClass<*>? = null
		private var serialize: (RaptorGraphScope.(value: Any) -> Any)? = null
		private var valueClass: KClass<*>? = null


		internal fun build(): GraphAliasDefinition<*, *> {
			val valueClass = checkNotNull(valueClass) { "Conversion must be defined: conversion { … }" }
			val referencedValueClass = checkNotNull(referencedValueClass)
			val parse = checkNotNull(parse)
			val serialize = checkNotNull(serialize)

			@Suppress("UNCHECKED_CAST")
			return GraphAliasDefinition(
				parse = parse,
				referencedValueClass = referencedValueClass as KClass<Any>,
				serialize = serialize,
				stackTrace = stackTrace,
				valueClass = valueClass as KClass<Any>
			)
		}

		fun <Value : Any, OtherValue : Any> conversion(
			valueClass: KClass<Value>,
			referencedValueClass: KClass<OtherValue>,
			configure: Conversion<Value, OtherValue>.() -> Unit
		) {
			check(this.valueClass === null) { "Cannot define multiple conversions." }

			this.referencedValueClass = referencedValueClass as KClass<Any>
			@Suppress("UNCHECKED_CAST")
			this.valueClass = valueClass as KClass<Any>

			Conversion<Value, OtherValue>().configure()

			checkNotNull(parse) { "GraphQL parsing must be defined: parse { … }" }
			checkNotNull(serialize) { "GraphQL serializing must be defined: serialize { … }" }
		}


		inline fun <reified Value : Any> conversion(@BuilderInference noinline configure: Conversion<Value, Any>.() -> Unit) =
			conversion(valueClass = Value::class, configure = configure)


		fun <Value : Any> conversion(valueClass: KClass<Value>, configure: Conversion<Value, Any>.() -> Unit) {
			check(this.valueClass === null) { "Cannot define multiple conversions." }

			@Suppress("UNCHECKED_CAST")
			this.valueClass = valueClass as KClass<Any>

			Conversion<Value, Any>().configure()

			checkNotNull(parse) { "GraphQL parsing must be defined: parse { … }" }
			checkNotNull(serialize) { "JSON serializing must be defined: serialize { … }" }
		}


		inner class Conversion<Value : Any, OtherValue : Any> internal constructor() {

			fun parse(parse: RaptorGraphScope.(input: OtherValue) -> Value?) {
				check(this@Builder.parse === null) { "Cannot define multiple GraphQL parsers." }

				this@Builder.parse = parse as RaptorGraphScope.(input: Any) -> Any?
			}


			fun serialize(serialize: RaptorGraphScope.(value: Value) -> OtherValue) {
				check(this@Builder.serialize === null) { "Cannot define multiple GraphQL serializers." }

				this@Builder.serialize = serialize as RaptorGraphScope.(value: Any) -> Any
			}
		}
	}
}


class GraphArgumentDefinition<Value> internal constructor(
	internal val default: GValue?, // FIXME do not expose GValue - add value/obj builder
	name: String?,
	internal val valueType: KType // FIXME class + nullability
) {

	private var isProvided = false

	internal var name = name // FIXME validate uniqueness
		private set


	operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): Reference<Value> {
		check(!isProvided) { "Cannot delegate multiple variables to the same argument." }

		val name = name ?: property.name

		this.isProvided = true
		this.name = name

		return object : Reference<Value> {

			override fun getValue(thisRef: Any?, property: KProperty<*>): Value =
				GraphInputContext.current.argument(name)
		}
	}


	class Builder<Value> internal constructor(
		private val valueType: KType
	) {

		private var default: GValue? = null
		private var name: String? = null


		internal fun build() =
			GraphArgumentDefinition<Value>(
				default = default,
				name = name,
				valueType = valueType
			)


		private fun default(default: GValue) {
			check(this.default === null) { "Cannot define multiple defaults." }

			this.default = default
		}


		// FIXME support List and InputObject
		// FIXME this is annoying, esp. Enum. can't we automate that?

		fun defaultNull() =
			default(GNullValue.withoutOrigin)


		fun defaultBoolean(default: Boolean) =
			default(GBooleanValue(default))


		fun defaultEnumValue(default: String) =
			default(GEnumValue(default))


		fun defaultFloat(default: Double) =
			default(GFloatValue(default))


		fun defaultInt(default: Int) =
			default(GIntValue(default))


		fun defaultString(default: String) =
			default(GStringValue(default))


		fun name(name: String) {
			check(this.name === null) { "Cannot define multiple names." }

			this.name = name
		}
	}


	interface Reference<out Value> {

		operator fun getValue(thisRef: Any?, property: KProperty<*>): Value
	}
}


class GraphEnumDefinition<Value : Enum<Value>> internal constructor(
	internal val description: String?,
	name: String,
	stackTrace: List<StackTraceElement>,
	valueClass: KClass<Value>,
	internal val values: List<Value>
) : GraphNamedTypeDefinition<Value>(
	name = name,
	stackTrace = stackTrace,
	valueClass = valueClass
) {

	override fun toString() =
		toString("enum '$name' ${valueClass.qualifiedName}")


	// FIXME value definitions
	class Builder internal constructor(
		private val stackTrace: List<StackTraceElement>
	) {

		private var description: String? = null
		private var valueClass: KClass<Enum<*>>? = null
		private var values: List<Enum<*>>? = null
		private var name: String? = null


		internal fun build(): GraphEnumDefinition<*> {
			val valueClass = checkNotNull(valueClass) { "Conversion must be defined: conversion { … }" }
			val values = checkNotNull(values)

			@Suppress("UNCHECKED_CAST")
			return GraphEnumDefinition(
				description = description,
				name = name ?: valueClass.simpleName ?: error("Cannot derive name from Kotlin class. It must be defined explicitly: name(\"…\")"),
				stackTrace = stackTrace,
				valueClass = valueClass as KClass<Dummy>,
				values = values as List<Dummy>
			)
		}


		inline fun <reified Value : Enum<Value>> conversion() =
			conversion(valueClass = Value::class, values = enumValues<Value>().toList())


		fun <Value : Enum<Value>> conversion(valueClass: KClass<Value>, values: List<Value>) {
			check(this.valueClass === null) { "Cannot define multiple conversions." }

			@Suppress("UNCHECKED_CAST")
			this.valueClass = valueClass as KClass<Enum<*>>
			this.values = values
		}


		fun description(description: String) {
			check(this.description === null) { "Cannot define multiple descriptions." }

			this.description = description
		}


		fun name(name: String) {
			check(this.name === null) { "Cannot define multiple names." }

			this.name = name
		}
	}


	private enum class Dummy
}


class GraphInputObjectDefinition<Value : Any> internal constructor(
	internal val arguments: List<GraphArgumentDefinition<*>>,
	internal val construct: RaptorGraphScope.() -> Value,
	internal val description: String?,
	name: String,
	stackTrace: List<StackTraceElement>,
	valueClass: KClass<Value>
) : GraphNamedTypeDefinition<Value>(
	name = name,
	stackTrace = stackTrace,
	valueClass = valueClass
) {

	override fun toString() =
		toString("input '$name' ${valueClass.qualifiedName}")


	class Builder internal constructor(
		private val stackTrace: List<StackTraceElement>
	) {

		private val arguments = mutableListOf<GraphArgumentDefinition<*>>()
		private var construct: (RaptorGraphScope.() -> Any)? = null
		private var description: String? = null
		private var name: String? = null
		private var valueClass: KClass<*>? = null


		internal fun build(): GraphInputObjectDefinition<*> {
			val valueClass = checkNotNull(valueClass) { "Conversion must be defined: conversion { … }" }
			val construct = checkNotNull(construct)

			@Suppress("UNCHECKED_CAST")
			return GraphInputObjectDefinition(
				arguments = arguments,
				construct = construct,
				description = description,
				name = name ?: valueClass.simpleName ?: error("Cannot derive name from Kotlin class. It must be defined explicitly: name(\"…\")"),
				stackTrace = stackTrace,
				valueClass = valueClass as KClass<Any>
			)
		}


		inline fun <reified Value : Any> conversion(@BuilderInference noinline configure: Conversion<Value>.() -> Unit) =
			conversion(valueClass = Value::class, configure = configure)


		fun <Value : Any> conversion(valueClass: KClass<Value>, configure: Conversion<Value>.() -> Unit) {
			check(this.valueClass === null) { "Cannot define multiple conversions." }

			this.valueClass = valueClass

			Conversion<Value>().configure()

			check(construct !== null) { "The constructor must be defined: construct { … }." }
			check(arguments.isNotEmpty()) { "At least one argument must be defined: argument<…>(…) { … }" }
		}


		fun description(description: String) {
			check(this.description === null) { "Cannot define multiple descriptions." }

			this.description = description
		}


		fun name(name: String) {
			check(this.name === null) { "Cannot define multiple names." }

			this.name = name
		}


		inner class Conversion<Value : Any> internal constructor() {

			@OptIn(ExperimentalStdlibApi::class)
			inline fun <reified ArgumentValue> argument(
				noinline configure: GraphArgumentDefinition.Builder<ArgumentValue>.() -> Unit = {}
			) =
				argument(valueType = typeOf<ArgumentValue>(), configure = configure)


			fun <ArgumentValue> argument(
				valueType: KType,
				configure: GraphArgumentDefinition.Builder<ArgumentValue>.() -> Unit = {}
			): GraphArgumentDefinition<ArgumentValue> {
				if (arguments.any { it.name === name })
					error("Cannot define multiple arguments named '$name'.")

				return GraphArgumentDefinition.Builder<ArgumentValue>(valueType = valueType)
					.apply(configure)
					.build()
					.also { arguments += it }
			}


			fun construct(construct: RaptorGraphScope.() -> Value) {
				check(this@Builder.construct === null) { "Cannot define multiple constructions." }

				this@Builder.construct = construct
			}
		}
	}
}


class GraphInterfaceDefinition<Value : Any> internal constructor(
	internal val description: String?,
	internal val fields: List<Field<Value, *>>,
	name: String,
	stackTrace: List<StackTraceElement>,
	valueClass: KClass<Value>
) : GraphNamedTypeDefinition<Value>(
	name = name,
	stackTrace = stackTrace,
	valueClass = valueClass
) {

	override fun toString() =
		toString("interface '$name' ${valueClass.qualifiedName}")


	class Builder internal constructor(
		private val stackTrace: List<StackTraceElement>
	) {

		private var description: String? = null
		private val fields = mutableListOf<Field<*, *>>()
		private var name: String? = null
		private var valueClass: KClass<*>? = null


		// FIXME check that valueClass !== otherVC
		internal fun build(): GraphInterfaceDefinition<*> {
			val valueClass = checkNotNull(valueClass) { "Conversion must be defined: conversion { … }" }

			@Suppress("UNCHECKED_CAST")
			return GraphInterfaceDefinition(
				description = description,
				fields = fields as List<Field<Any, *>>,
				name = name ?: valueClass.simpleName ?: error("Cannot derive name from Kotlin class. It must be defined explicitly: name(\"…\")"),
				stackTrace = stackTrace,
				valueClass = valueClass as KClass<Any>
			)
		}


		inline fun <reified Value : Any> conversion(noinline configure: Conversion<Value>.() -> Unit) =
			conversion(valueClass = Value::class, configure = configure)


		fun <Value : Any> conversion(valueClass: KClass<Value>, configure: Conversion<Value>.() -> Unit) {
			check(this.valueClass === null) { "Cannot define multiple conversions." }

			this.valueClass = valueClass

			Conversion<Value>().configure()

			check(fields.isNotEmpty()) { "At least one field must be defined: field(…) { … }" }
		}


		fun description(description: String) {
			check(this.description === null) { "Cannot define multiple descriptions." }

			this.description = description
		}


		fun name(name: String) {
			check(this.name === null) { "Cannot define multiple names." }

			this.name = name
		}


		inner class Conversion<Value : Any> internal constructor() {

			@OptIn(ExperimentalStdlibApi::class)
			inline fun <reified FieldValue> field(
				name: String,
				noinline configure: FieldBuilder<FieldValue>.() -> Unit = {}
			) =
				field(name = name, valueType = typeOf<FieldValue>(), configure = configure)


			inline fun <FieldValue> field(
				property: KProperty1<Value, FieldValue>,
				noinline configure: FieldBuilder<FieldValue>.() -> Unit = {}
			) =
				field(name = property.name, valueType = property.returnType, configure = configure)


			inline fun <FieldValue> field(
				function: KSuspendFunction2<Value, RaptorGraphScope, FieldValue>,
				noinline configure: FieldBuilder<FieldValue>.() -> Unit = {}
			) =
				field(name = function.name, valueType = function.returnType, configure = configure)


			fun <FieldValue> field(
				name: String,
				valueType: KType,
				configure: FieldBuilder<FieldValue>.() -> Unit = {}
			) {
				if (fields.any { it.name === name })
					error("Cannot define multiple fields named '$name'.")

				fields += FieldBuilder<FieldValue>(
					name = name,
					valueType = valueType
				)
					.apply(configure)
					.build()
			}


			inner class FieldBuilder<FieldValue> internal constructor(
				private val name: String,
				private val valueType: KType
			) {

				private val arguments = mutableListOf<GraphArgumentDefinition<*>>()
				private var description: String? = null
				private var isNullable = valueType.isMarkedNullable


				@OptIn(ExperimentalStdlibApi::class)
				inline fun <reified ArgumentValue> argument(
					noinline configure: GraphArgumentDefinition.Builder<ArgumentValue>.() -> Unit = {}
				) =
					argument(valueType = typeOf<ArgumentValue>(), configure = configure)


				fun <ArgumentValue> argument(
					valueType: KType,
					configure: GraphArgumentDefinition.Builder<ArgumentValue>.() -> Unit = {}
				): GraphArgumentDefinition<ArgumentValue> {
					if (arguments.any { it.name === name })
						error("Cannot define multiple arguments named '$name'.")

					return GraphArgumentDefinition.Builder<ArgumentValue>(valueType = valueType)
						.apply(configure)
						.build()
						.also { arguments += it }
				}


				internal fun build() =
					Field<Any, Any?>(
						arguments = arguments,
						description = description,
						name = name,
						valueType = valueType.withNullability(isNullable)
					)


				fun description(description: String) {
					check(this.description === null) { "Cannot define the description more than once." }

					this.description = description
				}
			}
		}
	}


	internal class Field<Parent : Any, Value>(
		val arguments: List<GraphArgumentDefinition<*>>,
		val description: String?,
		val name: String,
		val valueType: KType
	)
}


class GraphInterfaceExtensionDefinition<Value : Any> internal constructor(
	internal val fields: List<GraphInterfaceDefinition.Field<Value, *>>,
	stackTrace: List<StackTraceElement>,
	internal val valueClass: KClass<Value>
) : RaptorGraphDefinition(
	stackTrace = stackTrace
) {

	override fun toString() =
		toString("extension for interface Kotlin $valueClass")


	class Builder<Value : Any> internal constructor(
		private val stackTrace: List<StackTraceElement>,
		private val valueClass: KClass<Value>
	) {

		private val fields = mutableListOf<GraphInterfaceDefinition.Field<Value, *>>()


		// FIXME require at least one field
		internal fun build() =
			GraphInterfaceExtensionDefinition(
				fields = fields,
				stackTrace = stackTrace,
				valueClass = valueClass
			)


		fun <FieldValue> field(
			property: KProperty1<Value, FieldValue>,
			configure: FieldBuilder<FieldValue>.() -> Unit = {}
		) {
			val name = property.name

			if (fields.any { it.name === name })
				error("Cannot define multiple fields named '$name'.")

			fields += FieldBuilder<FieldValue>(
				name = name,
				valueType = property.returnType
			)
				.apply(configure)
				.build()
		}


		fun <FieldValue> field(
			function: KSuspendFunction2<Value, RaptorGraphScope, FieldValue>,
			configure: FieldBuilder<FieldValue>.() -> Unit = {}
		) {
			val name = function.name

			if (fields.any { it.name === name })
				error("Cannot define multiple fields named '$name'.")

			fields += FieldBuilder<FieldValue>(
				name = name,
				valueType = function.returnType
			)
				.apply(configure)
				.build()
		}


		@OptIn(ExperimentalStdlibApi::class)
		inline fun <reified FieldValue> field(
			name: String,
			@BuilderInference noinline configure: FieldBuilder<FieldValue>.() -> Unit
		) =
			field(name = name, valueType = typeOf<FieldValue>(), configure = configure)


		fun <FieldValue> field(
			name: String,
			valueType: KType,
			configure: FieldBuilder<FieldValue>.() -> Unit
		) {
			if (fields.any { it.name === name })
				error("Cannot define multiple fields named '$name'.")

			fields += FieldBuilder<FieldValue>(
				name = name,
				valueType = valueType
			)
				.apply(configure)
				.build()
		}


		inner class FieldBuilder<FieldValue> internal constructor(
			private val name: String,
			private val valueType: KType
		) {

			private val arguments = mutableListOf<GraphArgumentDefinition<*>>()
			private var description: String? = null
			private var isNullable = valueType.isMarkedNullable


			@OptIn(ExperimentalStdlibApi::class)
			inline fun <reified ArgumentValue> argument(
				noinline configure: GraphArgumentDefinition.Builder<ArgumentValue>.() -> Unit = {}
			) =
				argument(valueType = typeOf<ArgumentValue>(), configure = configure)


			fun <ArgumentValue> argument(
				valueType: KType,
				configure: GraphArgumentDefinition.Builder<ArgumentValue>.() -> Unit = {}
			): GraphArgumentDefinition<ArgumentValue> {
				if (arguments.any { it.name === name })
					error("Cannot define multiple arguments named '$name'.")

				return GraphArgumentDefinition.Builder<ArgumentValue>(valueType = valueType)
					.apply(configure)
					.build()
					.also { arguments += it }
			}


			internal fun build() =
				GraphInterfaceDefinition.Field<Value, FieldValue>(
					arguments = arguments,
					description = description,
					name = name,
					valueType = valueType.withNullability(isNullable)
				)


			fun description(description: String) {
				check(this.description === null) { "Cannot define the description more than once." }

				this.description = description
			}
		}
	}
}


class GraphObjectDefinition<Value : Any> internal constructor(
	internal val description: String?,
	internal val fields: List<Field<Value, *>>,
	name: String,
	stackTrace: List<StackTraceElement>,
	valueClass: KClass<Value>
) : GraphNamedTypeDefinition<Value>(
	name = name,
	stackTrace = stackTrace,
	valueClass = valueClass
) {

	override fun toString() =
		toString("type '$name' ${valueClass.qualifiedName}")


	class Builder internal constructor(
		private val stackTrace: List<StackTraceElement>
	) {

		private var description: String? = null
		private val fields = mutableListOf<Field<*, *>>()
		private var name: String? = null
		private var valueClass: KClass<*>? = null


		internal fun build(): GraphObjectDefinition<*> {
			val valueClass = checkNotNull(valueClass) { "Conversion must be defined: conversion { … }" }

			@Suppress("UNCHECKED_CAST")
			return GraphObjectDefinition(
				description = description,
				fields = fields as List<Field<Any, *>>,
				name = name ?: valueClass.simpleName ?: error("Cannot derive name from Kotlin class. It must be defined explicitly: name(\"…\")"),
				stackTrace = stackTrace,
				valueClass = valueClass as KClass<Any>
			)
		}


		inline fun <reified Value : Any> conversion(noinline configure: Conversion<Value>.() -> Unit) =
			conversion(valueClass = Value::class, configure = configure)


		fun <Value : Any> conversion(valueClass: KClass<Value>, configure: Conversion<Value>.() -> Unit) {
			check(this.valueClass === null) { "Cannot define multiple conversions." }

			this.valueClass = valueClass

			Conversion<Value>().configure()

			check(fields.isNotEmpty()) { "At least one field must be defined: field(…) { … }" }
		}


		fun description(description: String) {
			check(this.description === null) { "Cannot define multiple descriptions." }

			this.description = description
		}


		fun name(name: String) {
			check(this.name === null) { "Cannot define multiple names." }

			this.name = name
		}


		inner class Conversion<Value : Any> internal constructor() {

			fun <FieldValue> field(
				property: KProperty1<Value, FieldValue>,
				configure: FieldBuilder<FieldValue>.() -> Unit = {}
			) {
				val name = property.name

				if (fields.any { it.name === name })
					error("Cannot define multiple fields named '$name'.")

				fields += FieldBuilder<FieldValue>(
					name = name,
					valueType = property.returnType,
					implicitResolve = { property.get(it) }
				)
					.apply(configure)
					.build()
			}


			fun <FieldValue> field(
				function: KSuspendFunction2<Value, RaptorGraphScope, FieldValue>,
				configure: FieldBuilder<FieldValue>.() -> Unit = {}
			) {
				val name = function.name

				if (fields.any { it.name === name })
					error("Cannot define multiple fields named '$name'.")

				fields += FieldBuilder<FieldValue>(
					name = name,
					valueType = function.returnType,
					implicitResolve = { function.invoke(it, this) }
				)
					.apply(configure)
					.build()
			}


			@OptIn(ExperimentalStdlibApi::class)
			inline fun <reified FieldValue> field(
				name: String,
				@BuilderInference noinline configure: FieldBuilder<FieldValue>.() -> Unit
			) =
				field(name = name, valueType = typeOf<FieldValue>(), configure = configure)


			fun <FieldValue> field(
				name: String,
				valueType: KType,
				configure: FieldBuilder<FieldValue>.() -> Unit
			) {
				if (fields.any { it.name === name })
					error("Cannot define multiple fields named '$name'.")

				fields += FieldBuilder<FieldValue>(
					name = name,
					valueType = valueType
				)
					.apply(configure)
					.build()
			}


			inner class FieldBuilder<FieldValue> internal constructor(
				private val name: String,
				private val valueType: KType,
				implicitResolve: (suspend RaptorGraphScope.(parent: Value) -> FieldValue?)? = null
			) {

				private val arguments = mutableListOf<GraphArgumentDefinition<*>>()
				private var description: String? = null
				private var isImplicitResolver = implicitResolve !== null
				private var isNullable = valueType.isMarkedNullable
				private var resolve: (suspend RaptorGraphScope.(parent: Any) -> Any?)? =
					implicitResolve as (suspend RaptorGraphScope.(parent: Any) -> Any?)?


				@OptIn(ExperimentalStdlibApi::class)
				inline fun <reified ArgumentValue> argument(
					noinline configure: GraphArgumentDefinition.Builder<ArgumentValue>.() -> Unit = {}
				) =
					argument(valueType = typeOf<ArgumentValue>(), configure = configure)


				fun <ArgumentValue> argument(
					valueType: KType,
					configure: GraphArgumentDefinition.Builder<ArgumentValue>.() -> Unit = {}
				): GraphArgumentDefinition<ArgumentValue> {
					if (arguments.any { it.name === name })
						error("Cannot define multiple arguments named '$name'.")

					return GraphArgumentDefinition.Builder<ArgumentValue>(valueType = valueType)
						.apply(configure)
						.build()
						.also { arguments += it }
				}


				internal fun build() =
					Field(
						arguments = arguments,
						description = description,
						name = name,
						resolve = resolve,
						valueType = valueType.withNullability(isNullable)
					)


				fun description(description: String) {
					check(this.description === null) { "Cannot define the description more than once." }

					this.description = description
				}


				@Suppress("UNCHECKED_CAST")
				fun resolve(resolve: suspend RaptorGraphScope.(parent: Value) -> FieldValue) {
					check(this.resolve === null && !this.isImplicitResolver) { "Cannot define multiple resolutions." }

					this.isImplicitResolver = false
					this.resolve = resolve as suspend RaptorGraphScope.(parent: Any) -> Any?
				}


				// remove once fixed: https://youtrack.jetbrains.com/issue/KT-36371
				@Suppress("UNCHECKED_CAST")
				fun resolveNullable(resolve: suspend RaptorGraphScope.(parent: Value) -> FieldValue?) {
					check(this.resolve === null && !this.isImplicitResolver) { "Cannot define multiple resolutions." }

					this.isImplicitResolver = false
					this.isNullable = true
					this.resolve = resolve as suspend RaptorGraphScope.(parent: Any) -> Any?
				}
			}
		}
	}


	internal class Field<in Parent : Any, Value>(
		val arguments: List<GraphArgumentDefinition<*>>,
		val description: String?,
		val name: String,
		val resolve: (suspend RaptorGraphScope.(parent: Parent) -> Value)?,
		val valueType: KType
	)
}


class GraphObjectExtensionDefinition<Value : Any> internal constructor(
	internal val fields: List<GraphObjectDefinition.Field<Value, *>>,
	stackTrace: List<StackTraceElement>,
	internal val valueClass: KClass<Value>
) : RaptorGraphDefinition(
	stackTrace = stackTrace
) {

	override fun toString() =
		toString("extension for object of Kotlin $valueClass")


	class Builder<Value : Any> internal constructor(
		private val stackTrace: List<StackTraceElement>,
		private val valueClass: KClass<Value>
	) {

		private val fields = mutableListOf<GraphObjectDefinition.Field<Value, *>>()


		// FIXME require at least one field
		internal fun build() =
			GraphObjectExtensionDefinition(
				fields = fields,
				stackTrace = stackTrace,
				valueClass = valueClass
			)


		fun <FieldValue> field(
			property: KProperty1<Value, FieldValue>,
			configure: FieldBuilder<FieldValue>.() -> Unit = {}
		) {
			val name = property.name

			if (fields.any { it.name === name })
				error("Cannot define multiple fields named '$name'.")

			fields += FieldBuilder<FieldValue>(
				name = name,
				valueType = property.returnType,
				implicitResolve = { property.get(it) }
			)
				.apply(configure)
				.build()
		}


		fun <FieldValue> field(
			function: KSuspendFunction2<Value, RaptorGraphScope, FieldValue>,
			configure: FieldBuilder<FieldValue>.() -> Unit = {}
		) {
			val name = function.name

			if (fields.any { it.name === name })
				error("Cannot define multiple fields named '$name'.")

			fields += FieldBuilder<FieldValue>(
				name = name,
				valueType = function.returnType,
				implicitResolve = { function.invoke(it, this) }
			)
				.apply(configure)
				.build()
		}


		@OptIn(ExperimentalStdlibApi::class)
		inline fun <reified FieldValue> field(
			name: String,
			@BuilderInference noinline configure: FieldBuilder<FieldValue>.() -> Unit
		) =
			field(name = name, valueType = typeOf<FieldValue>(), configure = configure)


		fun <FieldValue> field(
			name: String,
			valueType: KType,
			configure: FieldBuilder<FieldValue>.() -> Unit
		) {
			if (fields.any { it.name === name })
				error("Cannot define multiple fields named '$name'.")

			fields += FieldBuilder<FieldValue>(
				name = name,
				valueType = valueType
			)
				.apply(configure)
				.build()
		}


		inner class FieldBuilder<FieldValue> internal constructor(
			private val name: String,
			private val valueType: KType,
			implicitResolve: (suspend RaptorGraphScope.(parent: Value) -> FieldValue?)? = null
		) {

			private val arguments = mutableListOf<GraphArgumentDefinition<*>>()
			private var description: String? = null
			private var isImplicitResolver = implicitResolve !== null
			private var isNullable = valueType.isMarkedNullable
			private var resolve: (suspend RaptorGraphScope.(parent: Value) -> FieldValue?)? = implicitResolve


			@OptIn(ExperimentalStdlibApi::class)
			inline fun <reified ArgumentValue> argument(
				noinline configure: GraphArgumentDefinition.Builder<ArgumentValue>.() -> Unit = {}
			) =
				argument(valueType = typeOf<ArgumentValue>(), configure = configure)


			fun <ArgumentValue> argument(
				valueType: KType,
				configure: GraphArgumentDefinition.Builder<ArgumentValue>.() -> Unit = {}
			): GraphArgumentDefinition<ArgumentValue> {
				if (arguments.any { it.name === name })
					error("Cannot define multiple arguments named '$name'.")

				return GraphArgumentDefinition.Builder<ArgumentValue>(valueType = valueType)
					.apply(configure)
					.build()
					.also { arguments += it }
			}


			internal fun build() =
				GraphObjectDefinition.Field(
					arguments = arguments,
					description = description,
					name = name,
					resolve = resolve,
					valueType = valueType.withNullability(isNullable)
				)


			fun description(description: String) {
				check(this.description === null) { "Cannot define the description more than once." }

				this.description = description
			}


			@Suppress("UNCHECKED_CAST")
			fun resolve(resolve: suspend RaptorGraphScope.(parent: Value) -> FieldValue) {
				check(this.resolve === null && !this.isImplicitResolver) { "Cannot define multiple resolutions." }

				this.isImplicitResolver = false
				this.resolve = resolve
			}


			// remove once fixed: https://youtrack.jetbrains.com/issue/KT-36371
			@Suppress("UNCHECKED_CAST")
			fun resolveNullable(resolve: suspend RaptorGraphScope.(parent: Value) -> FieldValue?) {
				check(this.resolve === null && !this.isImplicitResolver) { "Cannot define multiple resolutions." }

				this.isImplicitResolver = false
				this.isNullable = true
				this.resolve = resolve
			}
		}
	}
}


class GraphOperationDefinition<ReturnValue> internal constructor(
	internal val field: GraphObjectDefinition.Field<GraphOperation, ReturnValue>,
	stackTrace: List<StackTraceElement>,
	internal val type: GraphOperation.Type
) : RaptorGraphDefinition(
	stackTrace = stackTrace
) {

	override fun toString() =
		toString("$type '${field.name}' -> ${field.valueType}")


	class Builder internal constructor(
		private val name: String,
		private val type: GraphOperation.Type,
		private val stackTrace: List<StackTraceElement>
	) {

		private val arguments = mutableListOf<GraphArgumentDefinition<*>>()
		private var description: String? = null
		private var isNullable = false
		private var resolve: (suspend RaptorGraphScope.() -> Any?)? = null
		private var valueType: KType? = null


		internal fun build(): GraphOperationDefinition<*> {
			val valueType = checkNotNull(valueType) { "Conversion must be defined: conversion { … }" }
			val resolve = checkNotNull(resolve)

			return GraphOperationDefinition<Any?>(
				field = GraphObjectDefinition.Field(
					arguments = arguments,
					description = description,
					name = name,
					resolve = { resolve() },
					valueType = valueType.withNullability(isNullable)
				),
				stackTrace = stackTrace,
				type = type
			)
		}


		@OptIn(ExperimentalStdlibApi::class)
		inline fun <reified Value> conversion(@BuilderInference noinline configure: Conversion<Value>.() -> Unit) =
			conversion(valueType = typeOf<Value>(), configure = configure)


		fun <Value> conversion(valueType: KType, @BuilderInference configure: Conversion<Value>.() -> Unit) {
			check(this.valueType === null) { "Cannot define multiple conversions." }

			this.isNullable = valueType.isMarkedNullable
			this.valueType = valueType

			Conversion<Value>().configure()

			check(resolve !== null) { "The resolution must be defined: resolve { … }" }
		}


		fun description(description: String) {
			check(this.description === null) { "Cannot define the description more than once." }

			this.description = description
		}


		inner class Conversion<Value> internal constructor() {

			@OptIn(ExperimentalStdlibApi::class)
			inline fun <reified ArgumentValue> argument(
				noinline configure: GraphArgumentDefinition.Builder<ArgumentValue>.() -> Unit = {}
			) =
				argument(valueType = typeOf<ArgumentValue>(), configure = configure)


			fun <ArgumentValue> argument(
				valueType: KType,
				configure: GraphArgumentDefinition.Builder<ArgumentValue>.() -> Unit = {}
			): GraphArgumentDefinition<ArgumentValue> {
				// FIXME
//				if (arguments.any { it.name === name })
//					error("Cannot define multiple arguments named '$name'.")

				return GraphArgumentDefinition.Builder<ArgumentValue>(valueType = valueType)
					.apply(configure)
					.build()
					.also { arguments += it }
			}


			fun resolve(resolve: suspend RaptorGraphScope.() -> Value) {
				check(this@Builder.resolve === null) { "Cannot define multiple resolutions." }

				this@Builder.resolve = resolve
			}


			// remove once fixed: https://youtrack.jetbrains.com/issue/KT-36371
			@Suppress("UNCHECKED_CAST")
			fun resolveNullable(resolve: suspend RaptorGraphScope.() -> Value?) {
				check(this@Builder.resolve === null) { "Cannot define multiple resolutions." }

				isNullable = true
				this@Builder.resolve = resolve
			}
		}
	}
}


class GraphScalarDefinition<Value : Any> internal constructor(
	internal val description: String?,
	internal val jsonInputClass: KClass<*>,
	name: String,
	internal val parseBoolean: (RaptorGraphScope.(input: Boolean) -> Value?)?,
	internal val parseFloat: (RaptorGraphScope.(input: Double) -> Value?)?,
	internal val parseInt: (RaptorGraphScope.(input: Int) -> Value?)?,
	internal val parseJson: RaptorGraphScope.(input: Any) -> Value?,
	internal val parseObject: (RaptorGraphScope.(input: Map<String, *>) -> Any?)?,
	internal val parseString: (RaptorGraphScope.(input: String) -> Value?)?,
	internal val serializeJson: RaptorGraphScope.(value: Value) -> Any,
	stackTrace: List<StackTraceElement>,
	valueClass: KClass<Value>
) : GraphNamedTypeDefinition<Value>(
	name = name,
	stackTrace = stackTrace,
	valueClass = valueClass
) {

	override fun toString() =
		toString("scalar '$name' ${valueClass.qualifiedName}")


	class Builder internal constructor(
		private val stackTrace: List<StackTraceElement>
	) {

		private var description: String? = null
		private var jsonInputClass: KClass<*>? = null
		private var name: String? = null
		private var parseBoolean: (RaptorGraphScope.(input: Boolean) -> Any?)? = null
		private var parseFloat: (RaptorGraphScope.(input: Double) -> Any?)? = null
		private var parseObject: (RaptorGraphScope.(input: Map<String, *>) -> Any?)? = null
		private var parseInt: (RaptorGraphScope.(input: Int) -> Any?)? = null
		private var parseJson: (RaptorGraphScope.(input: Any) -> Any?)? = null
		private var parseString: (RaptorGraphScope.(input: String) -> Any?)? = null
		private var serializeJson: (RaptorGraphScope.(value: Any) -> Any)? = null
		private var valueClass: KClass<*>? = null


		internal fun build(): GraphScalarDefinition<*> {
			val valueClass = checkNotNull(valueClass) { "Conversion must be defined: conversion { … }" }
			val jsonInputClass = checkNotNull(jsonInputClass)
			val parseJson = checkNotNull(parseJson)
			val serializeJson = checkNotNull(serializeJson)

			@Suppress("UNCHECKED_CAST")
			return GraphScalarDefinition(
				description = description,
				jsonInputClass = jsonInputClass,
				name = name ?: valueClass.simpleName ?: error("Cannot derive name from Kotlin class. It must be defined explicitly: name(\"…\")"),
				parseBoolean = parseBoolean,
				parseFloat = parseFloat,
				parseObject = parseObject,
				parseInt = parseInt,
				parseJson = parseJson,
				parseString = parseString,
				serializeJson = serializeJson,
				stackTrace = stackTrace,
				valueClass = valueClass as KClass<Any>
			)
		}


		inline fun <reified Value : Any> conversion(@BuilderInference noinline configure: Conversion<Value>.() -> Unit) =
			conversion(valueClass = Value::class, configure = configure)


		fun <Value : Any> conversion(valueClass: KClass<Value>, configure: Conversion<Value>.() -> Unit) {
			check(this.valueClass === null) { "Cannot define multiple conversions." }

			@Suppress("UNCHECKED_CAST")
			this.valueClass = valueClass as KClass<Any>

			Conversion<Value>().configure()

			checkNotNull(parseBoolean ?: parseFloat ?: parseInt ?: parseObject ?: parseString) {
				"At least one GraphQL parsing function must be defined: parseBoolean/Int/Float/String { … }"
			}
			checkNotNull(parseJson) { "JSON parsing must be defined: parseJson { … }" }
			checkNotNull(serializeJson) { "JSON serializing must be defined: serializeJson { … }" }
		}


		fun description(description: String) {
			check(this.description === null) { "Cannot define multiple descriptions." }

			this.description = description
		}


		fun name(name: String) {
			check(this.name === null) { "Cannot define multiple names." }

			this.name = name
		}


		inner class Conversion<Value : Any> internal constructor() {

			fun parseBoolean(parse: KFunction1<Boolean, Value?>) =
				parseBoolean { input ->
					parse(input)
				}


			fun parseBoolean(parse: RaptorGraphScope.(input: Boolean) -> Value?) {
				check(parseBoolean === null) { "Cannot define multiple GraphQL Boolean parsers." }

				@Suppress("UNCHECKED_CAST")
				parseBoolean = parse
			}

			fun parseFloat(parse: KFunction1<Double, Value?>) =
				parseFloat { input ->
					parse(input)
				}


			fun parseFloat(parse: RaptorGraphScope.(input: Double) -> Value?) {
				check(parseFloat === null) { "Cannot define multiple GraphQL Float parsers." }

				@Suppress("UNCHECKED_CAST")
				parseFloat = parse
			}


			fun parseInt(parse: KFunction1<Int, Value?>) =
				parseInt { input ->
					parse(input)
				}


			fun parseInt(parse: RaptorGraphScope.(input: Int) -> Value?) {
				check(parseInt === null) { "Cannot define multiple GraphQL Int parsers." }

				@Suppress("UNCHECKED_CAST")
				parseInt = parse
			}


			// FIXME standardize
			inline fun <reified JsonInput : Any> parseJson(parse: KFunction1<JsonInput, Value?>) =
				parseJson(jsonInputClass = JsonInput::class) { input ->
					parse(input)
				}


			inline fun <reified JsonInput : Any> parseJson(noinline parse: RaptorGraphScope.(input: JsonInput) -> Value?) =
				parseJson(jsonInputClass = JsonInput::class, parse = parse)


			fun <JsonInput : Any> parseJson(jsonInputClass: KClass<out JsonInput>, parse: RaptorGraphScope.(input: JsonInput) -> Value?) {
				check(parseJson === null) { "Cannot define multiple JSON parsers." }

				this@Builder.jsonInputClass = jsonInputClass

				@Suppress("UNCHECKED_CAST")
				parseJson = parse as RaptorGraphScope.(input: Any) -> Any?
			}


			fun parseObject(parse: RaptorGraphScope.(input: Map<String, *>) -> Value?) {
				check(parseObject === null) { "Cannot define multiple GraphQL Object parsers." }

				@Suppress("UNCHECKED_CAST")
				parseObject = parse
			}


			fun parseString(parse: KFunction1<String, Value?>) {
				parseString { input ->
					parse(input)
				}
			}


			fun parseString(parse: RaptorGraphScope.(input: String) -> Value?) {
				check(parseString === null) { "Cannot define multiple GraphQL String parsers." }

				@Suppress("UNCHECKED_CAST")
				parseString = parse
			}


			fun serializeJson(serialize: KFunction1<Value, Any>) {
				serializeJson { value ->
					serialize(value)
				}
			}


			fun serializeJson(serialize: KProperty1<Value, Any>) {
				serializeJson { value ->
					serialize(value)
				}
			}


			fun serializeJson(serialize: RaptorGraphScope.(value: Value) -> Any) {
				check(serializeJson === null) { "Cannot define multiple JSON serializers." }

				@Suppress("UNCHECKED_CAST")
				serializeJson = serialize as RaptorGraphScope.(value: Any) -> Any
			}
		}
	}
}
