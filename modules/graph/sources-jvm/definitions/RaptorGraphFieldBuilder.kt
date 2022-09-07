package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*
import io.fluidsonic.stdlib.*


@RaptorDsl
@Suppress("EXPOSED_PROPERTY_TYPE_IN_CONSTRUCTOR_ERROR")
public open class RaptorGraphFieldBuilder internal constructor(
	protected val kotlinType: KotlinType,
	protected val name: String,
	parentKotlinType: KotlinType,
	protected val stackTrace: List<StackTraceElement>,
	protected val argumentContainer: RaptorGraphArgumentDefinitionBuilder.ContainerImpl = RaptorGraphArgumentDefinitionBuilder.ContainerImpl(
		factoryName = "-",
		parentKotlinType = parentKotlinType
	),
) : RaptorGraphArgumentDefinitionBuilder.ContainerInternal by argumentContainer {

	protected var description: String? = null


	internal open fun build(): GraphFieldDefinition =
		GraphFieldDefinition.Unresolvable(
			argumentDefinitions = argumentContainer.argumentDefinitions,
			description = description,
			kotlinType = kotlinType,
			name = name,
			stackTrace = stackTrace
		)


	@RaptorDsl
	public fun description(description: String) {
		check(this.description === null) { "Cannot define the description more than once." }

		this.description = description
	}


	@RaptorDsl
	public class WithResolver<Type, ParentType : Any> internal constructor(
		implicitResolver: (suspend RaptorGraphOutputScope.(parent: Any) -> Any?)? = null,
		kotlinType: KotlinType,
		name: String,
		parentKotlinType: KotlinType,
		stackTrace: List<StackTraceElement>,
		argumentContainer: RaptorGraphArgumentDefinitionBuilder.ContainerImpl = RaptorGraphArgumentDefinitionBuilder.ContainerImpl(
			factoryName = "resolver",
			parentKotlinType = parentKotlinType
		),
	) : RaptorGraphFieldBuilder(
		kotlinType = kotlinType,
		name = name,
		parentKotlinType = parentKotlinType,
		stackTrace = stackTrace,
		argumentContainer = argumentContainer
	) {

		private var isImplicitResolve = implicitResolver !== null
		private var resolve: (suspend RaptorGraphOutputScope.(parent: Any) -> Any?)? = implicitResolver


		override fun build(): GraphFieldDefinition {
			val resolve = resolve ?: error("A resolver must be defined: resolve { … }")

			return GraphFieldDefinition.Resolvable(
				argumentDefinitions = argumentContainer.argumentDefinitions,
				argumentResolver = argumentContainer.resolver,
				description = description,
				kotlinType = kotlinType,
				name = name,
				resolve = when (kotlinType.classifier) {
					RaptorUnion2::class -> {
						{ parent ->
							resolve(parent)?.cast<RaptorUnion2<*, *>>()?.value
						}
					}

					else -> resolve
				},
				stackTrace = stackTrace
			)
		}


		@RaptorDsl
		@Suppress("UNCHECKED_CAST")
		public fun resolver(resolve: suspend RaptorGraphOutputScope.(parent: ParentType) -> Type) {
			check(this.resolve === null && !this.isImplicitResolve) { "Cannot define multiple resolutions." }

			this.isImplicitResolve = false
			this.resolve = resolve as suspend RaptorGraphOutputScope.(parent: Any) -> Any?
		}
	}
}
