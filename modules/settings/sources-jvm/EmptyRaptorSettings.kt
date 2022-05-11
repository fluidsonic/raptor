package io.fluidsonic.raptor


internal object EmptyRaptorSettings : RaptorSettings {

	override fun valueProvider(path: String) =
		null
}
