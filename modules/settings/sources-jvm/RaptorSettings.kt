package io.fluidsonic.raptor


// FIXME inject with DI? or should this only be used at assembly-time?
// FIXME refactor
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
		public fun set(key: String, value: Int) {
			set(key, Value.of(value))
		}


		@RaptorDsl
		public fun set(key: String, value: String) {
			set(key, Value.of(value))
		}


		@RaptorDsl
		public fun set(key: String, value: Value) {
			values[key] = value
		}


		@RaptorDsl
		public infix fun String.by(value: Int) {
			this by Value.of(value)
		}


		@RaptorDsl
		public infix fun String.by(value: String) {
			this by Value.of(value)
		}


		@RaptorDsl
		public infix fun String.by(value: Value) {
			set(this, value)
		}


		@RaptorDsl
		public operator fun String.invoke(values: Builder.() -> Unit) {
			this@Builder.values[this] = Value.of(Builder().apply(values).build())
		}
	}


	public interface Value {

		public fun int(): Int
		public fun intList(): List<Int>
		public fun settings(): RaptorSettings
		public fun settingsList(): List<RaptorSettings>
		public fun string(): String
		public fun stringList(): List<String>


		public companion object {

			public fun of(string: Int): Value =
				IntValue(string)


			public fun of(string: String): Value =
				StringValue(string)


			public fun of(settings: RaptorSettings): Value =
				SettingsValue(settings)


			private class IntValue(val value: Int) : Value {

				override fun int() =
					value


				override fun intList() =
					listOf(value)


				override fun settings() =
					error("Int value cannot be converted to settings: $value")


				override fun settingsList() =
					error("Int value cannot be converted to settings: $value")


				override fun string() =
					error("Int value cannot be converted to string: $value")


				override fun stringList() =
					error("Int value cannot be converted to string: $value")
			}


			private class SettingsValue(private val value: RaptorSettings) : Value {

				override fun int() =
					error("Settings value cannot be converted to int: $value")


				override fun intList() =
					error("Settings value cannot be converted to int: $value")


				override fun settings() =
					value


				override fun settingsList() =
					listOf(value)


				override fun string() =
					error("Settings value cannot be converted to string: $value")


				override fun stringList() =
					error("Settings value cannot be converted to string: $value")
			}


			private class StringValue(val value: String) : Value {

				override fun int() =
					error("String value cannot be converted to int: $value")


				override fun intList() =
					error("String value cannot be converted to int: $value")


				override fun settings() =
					error("String value cannot be converted to settings: $value")


				override fun settingsList() =
					error("String value cannot be converted to settings: $value")


				override fun string() =
					value


				override fun stringList() =
					listOf(value)
			}
		}
	}
}


public fun RaptorSettings.int(path: String): Int =
	value(path).int()


public fun RaptorSettings.intList(path: String): List<Int> =
	value(path).intList()


public fun RaptorSettings.settings(path: String): RaptorSettings =
	value(path).settings()


public fun RaptorSettings.settingsList(path: String): List<RaptorSettings> =
	value(path).settingsList()


public operator fun RaptorSettings.get(path: String): RaptorSettings.Value? =
	valueOrNull(path)


public fun RaptorSettings.string(path: String): String =
	value(path).string()


public fun RaptorSettings.stringOrNull(path: String): String? =
	valueOrNull(path)?.string()


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
@Suppress("unused")
public fun RaptorGlobalDsl.settings(configure: RaptorSettings.Builder.() -> Unit): RaptorSettings =
	RaptorSettings.Builder().apply(configure).build()


// FIXME install(settings) w/o settings installed will lead the error below
@RaptorDsl
public val RaptorTopLevelConfigurationScope.settings: RaptorSettings
	get() = componentRegistry.root.oneOrNull(RaptorSettingsComponent.Key)?.settings
		?: error("No settings have been registered. Use install(settings) inside raptor { … } before any other configuration.")
