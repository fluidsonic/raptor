package io.fluidsonic.raptor

import io.fluidsonic.raptor.graphql.internal.*


@RaptorDsl
public class RaptorInputObjectGraphDefinitionBuilder<Type : Any> internal constructor(
	kotlinType: KotlinType,
	name: String,
	private val stackTrace: List<StackTraceElement>,
	private val argumentContainer: RaptorGraphArgumentDefinitionBuilder.ContainerImpl = RaptorGraphArgumentDefinitionBuilder.ContainerImpl(
		factoryName = "factory",
		parentKotlinType = kotlinType
	),
) :
	RaptorStructuredGraphTypeDefinitionBuilder<Type>(
		kotlinType = kotlinType,
		name = name
	),
	RaptorGraphArgumentDefinitionBuilder.Container by argumentContainer {

	private var create: (RaptorGraphInputScope.() -> Any)? = null


	override fun build(description: String?, additionalDefinitions: Collection<RaptorGraphDefinition>) =
		InputObjectGraphDefinition(
			additionalDefinitions = additionalDefinitions,
			argumentDefinitions = argumentContainer.argumentDefinitions.ifEmpty { null }
				?: error("At least one argument must be defined: argument<…>(…) { … }"),
			argumentResolver = argumentContainer.resolver,
			create = checkNotNull(create) { "The factory must be defined: factory { … }" },
			description = description,
			kotlinType = kotlinType,
			name = name,
			stackTrace = stackTrace
		)


	// FIXME rn
	@RaptorDsl
	public fun factory(create: RaptorGraphInputScope.() -> Type) {
		check(this.create === null) { "Cannot define multiple factories." }

		this.create = create
	}
}
