package io.fluidsonic.raptor


public interface RaptorSettings {

	public fun hasValue(path: String): Boolean
	public fun valueOrNull(path: String): Value?


	public companion object {

		public val empty: RaptorSettings = EmptyRaptorSettings


		public fun lookup(vararg settings: RaptorSettings): RaptorSettings =
			lookup(settings.toList())


		public fun lookup(settings: Iterable<RaptorSettings>): RaptorSettings =
			LookupRaptorSettings(settings.toList())
	}


	@RaptorDsl
	public class Builder internal constructor() {

		private val values: MutableMap<String, Value> = hashMapOf()


		internal fun build(): RaptorSettings {
			if (values.isEmpty())
				return empty

			return DefaultRaptorSettings(values = values.toMap(hashMapOf()))
		}


		@RaptorDsl
		public fun set(key: String, value: String) {
			values[key] = Value.of(value)
		}
	}


	public interface Value {

		public fun settings(): RaptorSettings
		public fun settingsList(): List<RaptorSettings>
		public fun string(): String
		public fun stringList(): List<String>


		public companion object {

			public fun of(string: String): Value =
				StringValue(string)


			private class StringValue(val value: String) : Value {

				override fun settings() =
					error("String value '$value' cannot be converted to settings.")


				override fun settingsList() =
					error("String value '$value' cannot be converted to settings.")


				override fun string() =
					value


				override fun stringList() =
					listOf(value)
			}
		}
	}
}


public fun RaptorSettings.settings(path: String): RaptorSettings =
	value(path).settings()


public fun RaptorSettings.settingsList(path: String): List<RaptorSettings> =
	value(path).settingsList()


public operator fun RaptorSettings.get(path: String): RaptorSettings.Value? =
	valueOrNull(path)


public fun RaptorSettings.string(path: String): String =
	value(path).string()


public fun RaptorSettings.stringList(path: String): List<String> =
	value(path).stringList()


public fun RaptorSettings.value(path: String): RaptorSettings.Value =
	valueOrNull(path) ?: error("Settings are missing a value for path '$path'.")


@RaptorDsl
public fun RaptorRootComponent.install(settings: RaptorSettings) {
	componentRegistry.oneOrNull(RaptorSettingsComponent.Key)?.let {
		error("Cannot set settings multiple times. Use RaptorSettings.lookup(…) to combine multiple settings.")
	}

	componentRegistry.register(RaptorSettingsComponent.Key, RaptorSettingsComponent(settings = settings))
}


@RaptorDsl
public fun raptorSettings(configure: RaptorSettings.Builder.() -> Unit): RaptorSettings =
	RaptorSettings.Builder().apply(configure).build()


@RaptorDsl
public val RaptorTopLevelConfigurationScope.settings: RaptorSettings
	get() = componentRegistry.root.oneOrNull(RaptorSettingsComponent.Key)?.settings
		?: error("No settings have been registered. Use install(settings) inside raptor { … } before any other configuration.")
