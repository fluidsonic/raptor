package io.fluidsonic.raptor


interface RaptorSettings {

	fun hasValue(path: String): Boolean
	fun valueOrNull(path: String): Value?


	companion object {

		val empty: RaptorSettings = EmptyRaptorSettings


		fun lookup(vararg settings: RaptorSettings): RaptorSettings =
			lookup(settings.toList())


		fun lookup(settings: Iterable<RaptorSettings>): RaptorSettings =
			LookupRaptorSettings(settings.toList())
	}


	object ExtensionKey : RaptorComponentExtensionKey<RaptorSettings> {

		override fun toString() = "settings"
	}


	interface Value {

		fun settings(): RaptorSettings
		fun settingsList(): List<RaptorSettings>
		fun string(): String
		fun stringList(): List<String>
	}
}


fun RaptorSettings.settings(path: String) =
	value(path).settings()


fun RaptorSettings.settingsList(path: String) =
	value(path).settingsList()


operator fun RaptorSettings.get(path: String): RaptorSettings.Value? =
	valueOrNull(path)


fun RaptorSettings.string(path: String) =
	value(path).string()


fun RaptorSettings.stringList(path: String) =
	value(path).stringList()


fun RaptorSettings.value(path: String) =
	valueOrNull(path) ?: error("Settings are missing a value for path '$path'.")


@RaptorDsl
fun RaptorRootComponent.install(settings: RaptorSettings) {
	componentRegistry.oneOrNull(RaptorSettingsComponent.Key)?.let {
		error("Cannot set settings multiple times. Use RaptorSettings.lookup(…) to combine multiple settings.")
	}

	componentRegistry.register(RaptorSettingsComponent.Key, RaptorSettingsComponent(settings = settings))
}


@RaptorDsl
val RaptorTopLevelConfigurationScope.settings: RaptorSettings
	get() = componentRegistry.root.oneOrNull(RaptorSettingsComponent.Key)?.settings
		?: error("No settings have been registered. Use install(settings) inside raptor { … } before any other configuration.")
