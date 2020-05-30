package io.fluidsonic.raptor

import kotlin.reflect.*


abstract class RaptorGraphNamedTypeDefinitionBuilder<Value : Any, Definition : GraphNamedTypeDefinition<Value>> internal constructor(
	private val defaultName: (() -> String?)? = null,
	protected val valueClass: KClass<Value>
) {

	private var description: String? = null
	private var name: String? = null


	init {
		checkGraphCompatibility(valueClass)
	}


	protected abstract fun build(description: String?, name: String): Definition


	internal fun build(defaultNamePrefix: String? = null) =
		build(
			description = description?.ifEmpty { null },
			name = name ?: defaultName?.invoke() ?: (defaultNamePrefix.orEmpty() + valueClass.defaultGraphName())
		)


	@RaptorDsl
	fun description(description: String) {
		check(this.description === null) { "Cannot define multiple descriptions." }

		this.description = description
	}


	@RaptorDsl
	fun name(name: String) {
		check(this.name === null) { "Cannot define multiple names." }

		this.name = name
	}
}
