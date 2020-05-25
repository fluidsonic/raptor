package io.fluidsonic.raptor

import com.typesafe.config.*


internal class HoconRaptorConfiguration private constructor(
	private val hocon: Config
) : RaptorConfiguration {

	constructor(resourcePath: String) :
		this(ConfigFactory.load(resourcePath))


	override fun hasValue(path: String) =
		hocon.hasPath(path)


	override fun valueOrNull(path: String): RaptorConfiguration.Value? =
		if (hocon.hasPath(path)) Value(hocon = hocon, path = path)
		else null


	private class Value(
		private val hocon: Config,
		private val path: String
	) : RaptorConfiguration.Value {

		override fun configuration() =
			HoconRaptorConfiguration(hocon = hocon.getConfig(path))


		override fun configurationList() =
			hocon.getConfigList(path).map(::HoconRaptorConfiguration)


		override fun string(): String =
			hocon.getString(path)


		override fun stringList(): List<String> =
			hocon.getStringList(path)
	}
}
