package io.fluidsonic.raptor

import com.typesafe.config.*


internal class HoconRaptorSettings private constructor(
	private val hocon: Config,
) : RaptorSettings {

	constructor(resourcePath: String) :
		this(ConfigFactory.load(resourcePath))


	override fun hasValue(path: String) =
		hocon.hasPath(path)


	override fun valueOrNull(path: String): RaptorSettings.Value? =
		if (hocon.hasPath(path)) Value(hocon = hocon, path = path)
		else null


	private class Value(
		private val hocon: Config,
		private val path: String,
	) : RaptorSettings.Value {

		override fun int(): Int =
			hocon.getInt(path)


		override fun intList(): List<Int> =
			hocon.getIntList(path)


		override fun settings() =
			HoconRaptorSettings(hocon = hocon.getConfig(path))


		override fun settingsList() =
			hocon.getConfigList(path).map(::HoconRaptorSettings)


		override fun string(): String =
			hocon.getString(path)


		override fun stringList(): List<String> =
			hocon.getStringList(path)
	}
}


public fun RaptorSettings.Companion.hocon(resourcePath: String): RaptorSettings =
	HoconRaptorSettings(resourcePath = resourcePath)
