package io.fluidsonic.raptor


private class ConstantRaptorSettingsValueProvider<out Value : Any>(
	override val value: Value?,
) : RaptorSettings.ValueProvider<Value> {

	override val description: String
		get() = "constant"
}


public fun <Value : Any> RaptorSettings.ValueProvider.Companion.constant(value: Value?): RaptorSettings.ValueProvider<Value> =
	ConstantRaptorSettingsValueProvider(value)
