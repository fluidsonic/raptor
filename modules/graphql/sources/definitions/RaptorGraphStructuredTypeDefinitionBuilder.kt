package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import kotlin.reflect.*


abstract class RaptorGraphStructuredTypeDefinitionBuilder<Value : Any, Definition : GraphNamedTypeDefinition<Value>> internal constructor(
	name: String,
	valueClass: KClass<Value>
) : RaptorGraphNamedTypeDefinitionBuilder<Value, Definition>(
	name = name,
	valueClass = valueClass
) {

	protected val nestedDefinitions: MutableList<RaptorGraphNamedTypeDefinitionBuilder<*, *>> = mutableListOf()


	protected abstract fun build(description: String?, nestedDefinitions: List<GraphNamedTypeDefinition<*>>): Definition


	final override fun build(description: String?) =
		build(
			description = description,
			nestedDefinitions = nestedDefinitions.map { it.build() }
		)


	@RaptorDsl
	fun nested(configure: NestedBuilder.() -> Unit) {
		NestedBuilder().apply(configure)
	}


	@RaptorDsl
	inner class NestedBuilder internal constructor() {

		// FIXME use global names (graphEnumDefinition) or else users may accidentally use the wrong one!
		@RaptorDsl
		inline fun <reified Value : Enum<Value>> enumDefinition(
			name: String = RaptorGraphDefinition.defaultName,
			@BuilderInference noinline configure: RaptorGraphEnumDefinitionBuilder<Value>.() -> Unit = {}
		) {
			enumDefinition(
				name = name,
				valueClass = Value::class,
				values = enumValues<Value>().toList(),
				configure = configure
			)
		}


		@RaptorDsl
		fun <Value : Enum<Value>> enumDefinition(
			name: String = RaptorGraphDefinition.defaultName,
			valueClass: KClass<Value>,
			values: List<Value>, // FIXME validate
			configure: RaptorGraphEnumDefinitionBuilder<Value>.() -> Unit = {}
		) {
			nestedDefinitions += RaptorGraphEnumDefinitionBuilder(
				name = RaptorGraphDefinition.resolveName(
					name,
					defaultNamePrefix = this@RaptorGraphStructuredTypeDefinitionBuilder.name,
					valueClass = valueClass
				),
				stackTrace = stackTrace(skipCount = 1),
				valueClass = valueClass,
				values = values
			)
				.apply(configure)
		}


		@RaptorDsl
		inline fun <reified Value : Any> inputObjectDefinition(
			name: String = RaptorGraphDefinition.defaultName,
			@BuilderInference noinline configure: RaptorGraphInputObjectDefinitionBuilder<Value>.() -> Unit
		) {
			inputObjectDefinition(
				name = name,
				valueClass = Value::class,
				configure = configure
			)
		}


		@RaptorDsl
		fun <Value : Any> inputObjectDefinition(
			name: String = RaptorGraphDefinition.defaultName,
			valueClass: KClass<Value>,
			configure: RaptorGraphInputObjectDefinitionBuilder<Value>.() -> Unit
		) {
			nestedDefinitions += RaptorGraphInputObjectDefinitionBuilder(
				name = RaptorGraphDefinition.resolveName(
					name,
					defaultNamePrefix = this@RaptorGraphStructuredTypeDefinitionBuilder.name,
					valueClass = valueClass
				),
				stackTrace = stackTrace(skipCount = 1),
				valueClass = valueClass
			)
				.apply(configure)
		}


		@RaptorDsl
		inline fun <reified Value : Any> interfaceDefinition(
			name: String = RaptorGraphDefinition.defaultName,
			@BuilderInference noinline configure: RaptorGraphInterfaceDefinitionBuilder<Value>.() -> Unit
		) {
			interfaceDefinition(
				name = name,
				valueClass = Value::class,
				configure = configure
			)
		}


		@RaptorDsl
		fun <Value : Any> interfaceDefinition(
			name: String = RaptorGraphDefinition.defaultName,
			valueClass: KClass<Value>,
			configure: RaptorGraphInterfaceDefinitionBuilder<Value>.() -> Unit
		) {
			nestedDefinitions += RaptorGraphInterfaceDefinitionBuilder(
				name = RaptorGraphDefinition.resolveName(
					name,
					defaultNamePrefix = this@RaptorGraphStructuredTypeDefinitionBuilder.name,
					valueClass = valueClass
				),
				stackTrace = stackTrace(skipCount = 1),
				valueClass = valueClass
			)
				.apply(configure)
		}


		@RaptorDsl
		inline fun <reified Value : Any> objectDefinition(
			name: String = RaptorGraphDefinition.defaultName,
			@BuilderInference noinline configure: RaptorGraphObjectDefinitionBuilder<Value>.() -> Unit
		) {
			objectDefinition(
				name = name,
				valueClass = Value::class,
				configure = configure
			)
		}


		@RaptorDsl
		fun <Value : Any> objectDefinition(
			name: String = RaptorGraphDefinition.defaultName,
			valueClass: KClass<Value>,
			configure: RaptorGraphObjectDefinitionBuilder<Value>.() -> Unit
		) {
			nestedDefinitions += RaptorGraphObjectDefinitionBuilder(
				name = RaptorGraphDefinition.resolveName(
					name,
					defaultNamePrefix = this@RaptorGraphStructuredTypeDefinitionBuilder.name,
					valueClass = valueClass
				),
				stackTrace = stackTrace(skipCount = 1),
				valueClass = valueClass
			)
				.apply(configure)
		}


		@RaptorDsl
		inline fun <reified Value : Any> scalarDefinition(
			name: String = RaptorGraphDefinition.defaultName,
			@BuilderInference noinline configure: RaptorGraphScalarDefinitionBuilder<Value>.() -> Unit
		) {
			scalarDefinition(
				name = name,
				valueClass = Value::class,
				configure = configure
			)
		}


		@RaptorDsl
		fun <Value : Any> scalarDefinition(
			name: String = RaptorGraphDefinition.defaultName,
			valueClass: KClass<Value>,
			configure: RaptorGraphScalarDefinitionBuilder<Value>.() -> Unit
		) {
			nestedDefinitions += RaptorGraphScalarDefinitionBuilder(
				name = RaptorGraphDefinition.resolveName(
					name,
					defaultNamePrefix = this@RaptorGraphStructuredTypeDefinitionBuilder.name,
					valueClass = valueClass
				),
				stackTrace = stackTrace(skipCount = 1),
				valueClass = valueClass
			)
				.apply(configure)
		}
	}
}
