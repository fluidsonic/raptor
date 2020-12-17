package io.fluidsonic.raptor

import io.fluidsonic.raptor.graphql.internal.*


@Suppress("EXPOSED_PROPERTY_TYPE_IN_CONSTRUCTOR")
public abstract class RaptorNamedGraphTypeDefinitionBuilder<Type : Any> internal constructor(
	protected val kotlinType: KotlinType,
	protected val name: String,
) {

	private var description: String? = null


	internal abstract fun build(description: String?): RaptorGraphDefinition


	internal fun build() =
		build(description = description?.ifEmpty { null })


	@RaptorDsl
	public fun description(description: String) {
		check(this.description === null) { "Cannot define multiple descriptions." }

		this.description = description
	}
}
