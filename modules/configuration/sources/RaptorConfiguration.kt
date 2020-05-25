package io.fluidsonic.raptor


interface RaptorConfiguration {

	fun hasValue(path: String): Boolean
	fun valueOrNull(path: String): Value?


	companion object {

		val empty: RaptorConfiguration = EmptyRaptorConfiguration


		fun lookup(vararg configurations: RaptorConfiguration): RaptorConfiguration =
			lookup(configurations.toList())


		fun lookup(configurations: Iterable<RaptorConfiguration>): RaptorConfiguration =
			LookupRaptorConfiguration(configurations.toList())
	}


	interface Value {

		fun configuration(): RaptorConfiguration
		fun configurationList(): List<RaptorConfiguration>
		fun string(): String
		fun stringList(): List<String>
	}
}


fun RaptorConfiguration.configuration(path: String) =
	value(path).configuration()


fun RaptorConfiguration.configurationList(path: String) =
	value(path).configurationList()


operator fun RaptorConfiguration.get(path: String): RaptorConfiguration.Value? =
	valueOrNull(path)


fun RaptorConfiguration.string(path: String) =
	value(path).string()


fun RaptorConfiguration.stringList(path: String) =
	value(path).stringList()


fun RaptorConfiguration.value(path: String) =
	valueOrNull(path) ?: error("Configuration is missing a value for path '$path'.")
