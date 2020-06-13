package io.fluidsonic.raptor

import kotlin.reflect.*


abstract class RaptorGraphNamedTypeDefinitionBuilder<Value : Any, Definition : GraphNamedTypeDefinition<Value>> internal constructor(
	protected val name: String,
	protected val valueClass: KClass<Value>
) {

	private var description: String? = null


	init {
		checkGraphCompatibility(valueClass)
	}


	protected abstract fun build(description: String?): Definition


	internal fun build() =
		build(description = description?.ifEmpty { null })


	@RaptorDsl
	fun description(description: String) {
		check(this.description === null) { "Cannot define multiple descriptions." }

		this.description = description
	}
}
