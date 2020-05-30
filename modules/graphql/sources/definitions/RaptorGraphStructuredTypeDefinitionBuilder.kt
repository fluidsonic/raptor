package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import kotlin.reflect.*


abstract class RaptorGraphStructuredTypeDefinitionBuilder<Value : Any, Definition : GraphNamedTypeDefinition<Value>> internal constructor(
	defaultName: (() -> String?)? = null,
	valueClass: KClass<Value>
) : RaptorGraphNamedTypeDefinitionBuilder<Value, Definition>(
	defaultName = defaultName,
	valueClass = valueClass
) {

	private val nestedDefinitions: MutableList<RaptorGraphNamedTypeDefinitionBuilder<*, *>> = mutableListOf()


	protected abstract fun build(description: String?, name: String, nestedDefinitions: List<GraphNamedTypeDefinition<*>>): Definition


	final override fun build(description: String?, name: String) =
		build(
			description = description,
			name = name,
			nestedDefinitions = nestedDefinitions.map { it.build(defaultNamePrefix = name) }
		)


	@RaptorDsl
	fun nested(configure: NestedBuilder.() -> Unit) {
		NestedBuilder().apply(configure)
	}


	@RaptorDsl
	inner class NestedBuilder internal constructor() {

		@RaptorDsl
		inline fun <reified Value : Enum<Value>> enumDefinition(
			@BuilderInference noinline configure: RaptorGraphEnumDefinitionBuilder<Value>.() -> Unit = {}
		) {
			enumDefinition(
				valueClass = Value::class,
				values = enumValues<Value>().toList(),
				configure = configure
			)
		}


		@RaptorDsl
		fun <Value : Enum<Value>> enumDefinition(
			valueClass: KClass<Value>,
			values: List<Value>, // FIXME validate
			configure: RaptorGraphEnumDefinitionBuilder<Value>.() -> Unit = {}
		) {
			nestedDefinitions += RaptorGraphEnumDefinitionBuilder(
				stackTrace = stackTrace(skipCount = 1),
				valueClass = valueClass,
				values = values
			)
				.apply(configure)
		}


		@RaptorDsl
		inline fun <reified Value : Any> inputObjectDefinition(
			@BuilderInference noinline configure: RaptorGraphInputObjectDefinitionBuilder<Value>.() -> Unit
		) {
			inputObjectDefinition(valueClass = Value::class, configure = configure)
		}


		@RaptorDsl
		fun <Value : Any> inputObjectDefinition(
			valueClass: KClass<Value>,
			configure: RaptorGraphInputObjectDefinitionBuilder<Value>.() -> Unit
		) {
			nestedDefinitions += RaptorGraphInputObjectDefinitionBuilder(
				stackTrace = stackTrace(skipCount = 1),
				valueClass = valueClass
			)
				.apply(configure)
		}


		@RaptorDsl
		inline fun <reified Value : Any> interfaceDefinition(
			@BuilderInference noinline configure: RaptorGraphInterfaceDefinitionBuilder<Value>.() -> Unit
		) {
			interfaceDefinition(valueClass = Value::class, configure = configure)
		}


		@RaptorDsl
		fun <Value : Any> interfaceDefinition(
			valueClass: KClass<Value>,
			configure: RaptorGraphInterfaceDefinitionBuilder<Value>.() -> Unit
		) {
			nestedDefinitions += RaptorGraphInterfaceDefinitionBuilder(
				stackTrace = stackTrace(skipCount = 1),
				valueClass = valueClass
			)
				.apply(configure)
		}


		@RaptorDsl
		inline fun <reified Value : Any> objectDefinition(
			@BuilderInference noinline configure: RaptorGraphObjectDefinitionBuilder<Value>.() -> Unit
		) {
			objectDefinition(valueClass = Value::class, configure = configure)
		}


		@RaptorDsl
		fun <Value : Any> objectDefinition(
			valueClass: KClass<Value>,
			configure: RaptorGraphObjectDefinitionBuilder<Value>.() -> Unit
		) {
			nestedDefinitions += RaptorGraphObjectDefinitionBuilder(
				stackTrace = stackTrace(skipCount = 1),
				valueClass = valueClass
			)
				.apply(configure)
		}


		@RaptorDsl
		inline fun <reified Value : Any> scalarDefinition(
			@BuilderInference noinline configure: RaptorGraphScalarDefinitionBuilder<Value>.() -> Unit
		) {
			scalarDefinition(valueClass = Value::class, configure = configure)
		}


		@RaptorDsl
		fun <Value : Any> scalarDefinition(
			valueClass: KClass<Value>,
			configure: RaptorGraphScalarDefinitionBuilder<Value>.() -> Unit
		) {
			nestedDefinitions += RaptorGraphScalarDefinitionBuilder(
				stackTrace = stackTrace(skipCount = 1),
				valueClass = valueClass
			)
				.apply(configure)
		}
	}
}
