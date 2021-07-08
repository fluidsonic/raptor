package io.fluidsonic.raptor


// FIXME Do we need to involve this in hasValue()?
private class EnvironmentRaptorSettingValue(
	private val name: String,
	private val default: String?,
) : RaptorSettings.Value {

	init {
		require(name.isNotBlank()) { "Name of environment variable cannot be blank." }
	}


	override fun int() =
		string().let { value ->
			value.toIntOrNull() ?: error("Environment variable '$name' cannot be converted to Int: $value")
		}


	override fun intList() =
		unsupportedConversion("List<Int>")


	override fun settings() =
		unsupportedConversion("RaptorSettings")


	override fun settingsList() =
		unsupportedConversion("List<RaptorSettings>")


	override fun string() =
		System.getenv(name) ?: default ?: error("Environment variable '$name' is not set.")


	override fun stringList() =
		unsupportedConversion("List<String>")


	private fun unsupportedConversion(type: String): Nothing =
		error("Cannot convert environment variable setting '$name' to $type. This is not supported.")
}


// FIXME does it make sense to actually do that as Value? valueOrNull() will then return non-null even if env var isn't set.
public fun RaptorSettings.Value.Companion.env(name: String, default: String? = null): RaptorSettings.Value =
	EnvironmentRaptorSettingValue(name = name, default = default)


@RaptorDsl
@Suppress("unused")
public fun RaptorSettings.Builder.env(name: String, default: String? = null): RaptorSettings.Value =
	RaptorSettings.Value.env(name = name, default = default)
