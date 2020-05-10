package io.fluidsonic.raptor

import io.fluidsonic.raptor.RaptorConfiguration.*


internal object EmptyRaptorConfiguration : RaptorConfiguration {

	override fun hasValue(path: String) =
		false


	override fun valueOrNull(path: String): Value? =
		null
}
