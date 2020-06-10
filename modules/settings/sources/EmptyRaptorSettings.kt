package io.fluidsonic.raptor

import io.fluidsonic.raptor.RaptorSettings.*


internal object EmptyRaptorSettings : RaptorSettings {

	override fun hasValue(path: String) =
		false


	override fun valueOrNull(path: String): Value? =
		null
}
