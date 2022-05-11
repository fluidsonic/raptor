package io.fluidsonic.raptor

import io.github.cdimascio.dotenv.*

// TODO Rework this & support multiplatform.
private val dotenv = dotenv { ignoreIfMissing = true }


private class EnvRaptorSettingValueProvider(
	private val name: String,
	default: String?,
) : RaptorSettings.ValueProvider<String> {

	init {
		require(name.isNotBlank()) { "Name of environment variable cannot be blank." }
	}

	override val value = dotenv[name] ?: default


	override val description: String
		get() = "environment variable '$name'"
}


public fun RaptorSettings.ValueProvider.Companion.env(name: String, default: String? = null): RaptorSettings.ValueProvider<String> =
	EnvRaptorSettingValueProvider(name = name, default = default)


@RaptorDsl
@Suppress("unused")
public fun RaptorSettings.Builder.env(name: String, default: String? = null): RaptorSettings.ValueProvider<String> =
	RaptorSettings.ValueProvider.env(name = name, default = default)
