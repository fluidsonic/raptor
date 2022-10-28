package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*


@RaptorDsl
public class RaptorEnumValueGraphDefinitionBuilder internal constructor(
	private val name: String,
) {

	private var description: String? = null


	internal fun build() =
		EnumValue(
			description = description?.ifEmpty { null },
			name = name,
		)


	@RaptorDsl
	public fun description(description: String) {
		check(this.description === null) { "Cannot define multiple descriptions." }

		this.description = description
	}
}
