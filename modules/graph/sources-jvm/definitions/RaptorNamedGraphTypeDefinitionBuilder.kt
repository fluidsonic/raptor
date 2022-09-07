package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*


@Suppress("EXPOSED_PROPERTY_TYPE_IN_CONSTRUCTOR_ERROR")
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
