package io.fluidsonic.raptor

import io.fluidsonic.graphql.*
import kotlin.reflect.*


@RaptorDsl
class RaptorGraphArgumentDefinitionBuilder<Value> internal constructor(
	private val valueType: KType
) {

	private var default: GValue? = null
	private var name: String? = null


	init {
		checkGraphCompatibility(valueType)
	}


	internal fun build() =
		GraphArgumentDefinition<Value>(
			default = default,
			name = name,
			valueType = valueType
		)


	@RaptorDsl
	private fun default(default: GValue) {
		check(this.default === null) { "Cannot define multiple defaults." }

		this.default = default
	}


	// FIXME support List and InputObject
	// FIXME this is annoying, esp. Enum. can't we automate that?

	@RaptorDsl
	fun defaultNull() =
		default(GNullValue.withoutOrigin)


	@RaptorDsl
	fun defaultBoolean(default: Boolean) =
		default(GBooleanValue(default))


	@RaptorDsl
	fun defaultEnumValue(default: String) =
		default(GEnumValue(default))


	@RaptorDsl
	fun defaultFloat(default: Double) =
		default(GFloatValue(default))


	@RaptorDsl
	fun defaultInt(default: Int) =
		default(GIntValue(default))


	@RaptorDsl
	fun defaultString(default: String) =
		default(GStringValue(default))


	@RaptorDsl
	fun name(name: String) {
		check(this.name === null) { "Cannot define multiple names." }

		this.name = name
	}


	interface Container {

		@RaptorDsl
		fun <ArgumentValue> argument(
			valueType: KType,
			configure: RaptorGraphArgumentDefinitionBuilder<ArgumentValue>.() -> Unit = {}
		): GraphArgumentDefinition<ArgumentValue>
	}


	internal interface ContainerInternal : Container {

		fun add(argument: GraphArgumentDefinition<*>)
	}


	internal class ContainerImpl : ContainerInternal {

		val arguments: MutableList<GraphArgumentDefinition<*>> = mutableListOf()


		override fun add(argument: GraphArgumentDefinition<*>) {
			// FIXME we need to evaluate all arguments lazily when the parent builder is done because provideDelegate won't be called yet and name is null
			if (arguments.any { it.name === argument.name })
				error("Cannot define multiple arguments named '${argument.name}'.")

			arguments += argument
		}


		@RaptorDsl
		override fun <ArgumentValue> argument(
			valueType: KType,
			configure: RaptorGraphArgumentDefinitionBuilder<ArgumentValue>.() -> Unit
		): GraphArgumentDefinition<ArgumentValue> {

			return RaptorGraphArgumentDefinitionBuilder<ArgumentValue>(valueType = valueType)
				.apply(configure)
				.build()
				.also(this::add)
		}
	}
}


@OptIn(ExperimentalStdlibApi::class)
@RaptorDsl
inline fun <reified ArgumentValue> RaptorGraphArgumentDefinitionBuilder.Container.argument(
	noinline configure: RaptorGraphArgumentDefinitionBuilder<ArgumentValue>.() -> Unit = {}
): GraphArgumentDefinition<ArgumentValue> =
	argument(valueType = typeOf<ArgumentValue>(), configure = configure)
