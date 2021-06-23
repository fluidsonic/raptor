package io.fluidsonic.raptor

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.graphql.internal.*
import io.fluidsonic.stdlib.*
import kotlin.reflect.*


@RaptorDsl
public class RaptorGraphArgumentDefinitionBuilder<Value> internal constructor(
	private val kotlinType: KotlinType,
	private val resolver: ArgumentResolver,
	private val stackTrace: List<StackTraceElement>,
) {

	private var default: GValue? = null
	private val isMaybe = kotlinType.classifier == Maybe::class
	private var name: String? = null


	internal fun build() =
		GraphArgumentDefinition(
			defaultValue = default,
			description = null, // FIXME
			kotlinType = kotlinType,
			name = name,
			resolver = resolver,
			stackTrace = stackTrace
		)


	@RaptorDsl
	private fun default(default: GValue) {
		check(!isMaybe) { "An optional argument of type '$kotlinType' cannot have a default value." }
		check(this.default === null) { "Cannot define multiple defaults." }

		this.default = default
	}


	// FIXME refactor

	@RaptorDsl
	public fun defaultList() {
		default(GListValue(emptyList()))
	}


	@RaptorDsl
	public fun defaultNull() {
		default(GNullValue.withoutOrigin)
	}


	@RaptorDsl
	public fun defaultBoolean(default: Boolean) {
		default(GBooleanValue(default))
	}


	@RaptorDsl
	public fun defaultEnumValue(default: String) {
		default(GEnumValue(default))
	}


	@RaptorDsl
	public fun defaultFloat(default: Double) {
		default(GFloatValue(default))
	}


	@RaptorDsl
	public fun defaultInt(default: Int) {
		default(GIntValue(default))
	}


	@RaptorDsl
	public fun defaultString(default: String) {
		default(GStringValue(default))
	}


	@RaptorDsl
	public fun name(name: String) {
		check(this.name === null) { "Cannot define multiple names." }

		this.name = name
	}


	public interface Container {

		@RaptorDsl
		public fun <ArgumentValue> argument(
			type: KType,
			configure: RaptorGraphArgumentDefinitionBuilder<ArgumentValue>.() -> Unit = {},
		): RaptorGraphArgumentDelegate<ArgumentValue>
	}


	internal interface ContainerInternal : Container {

		fun add(argument: GraphArgumentDefinition)
	}


	internal class ContainerImpl(
		private val parentKotlinType: KotlinType, // FIXME
		factoryName: String,
	) : ContainerInternal {

		val argumentDefinitions: MutableList<GraphArgumentDefinition> = mutableListOf()
		val resolver = ArgumentResolver(factoryName = factoryName)


		override fun add(argument: GraphArgumentDefinition) {
			// FIXME we need to evaluate all arguments lazily when the parent builder is done because provideDelegate won't be called yet and name is null
			if (argumentDefinitions.any { it.name === argument.name })
				error("Cannot define multiple arguments named '${argument.name}'.")

			argumentDefinitions += argument
		}


		@RaptorDsl
		override fun <ArgumentValue> argument(
			type: KType,
			configure: RaptorGraphArgumentDefinitionBuilder<ArgumentValue>.() -> Unit,
		): RaptorGraphArgumentDelegate<ArgumentValue> =
			RaptorGraphArgumentDefinitionBuilder<ArgumentValue>(
				kotlinType = KotlinType.of(
					type = type,
					containingType = parentKotlinType,
					allowMaybe = true,
					allowNull = true,
					allowedVariance = KVariance.OUT,
					requireSpecialization = true
				),
				resolver = resolver,
				stackTrace = stackTrace(skipCount = 1)
			)
				.apply(configure)
				.build()
				.also(this::add)
				.let {
					@Suppress("UNCHECKED_CAST")
					it as RaptorGraphArgumentDelegate<ArgumentValue>
				}


		@RaptorDsl
		internal fun <ArgumentValue> argument(
			type: KotlinType,
			configure: RaptorGraphArgumentDefinitionBuilder<ArgumentValue>.() -> Unit,
		): RaptorGraphArgumentDelegate<ArgumentValue> =
			RaptorGraphArgumentDefinitionBuilder<ArgumentValue>(
				kotlinType = type,
				resolver = resolver,
				stackTrace = stackTrace(skipCount = 1)
			)
				.apply(configure)
				.build()
				.also(this::add)
				.let {
					@Suppress("UNCHECKED_CAST")
					it as RaptorGraphArgumentDelegate<ArgumentValue>
				}
	}
}


@OptIn(ExperimentalStdlibApi::class)
@RaptorDsl
public inline fun <reified ArgumentValue> RaptorGraphArgumentDefinitionBuilder.Container.argument(
	noinline configure: RaptorGraphArgumentDefinitionBuilder<ArgumentValue>.() -> Unit = {},
): RaptorGraphArgumentDelegate<ArgumentValue> =
	argument(type = typeOf<ArgumentValue>(), configure = configure)
