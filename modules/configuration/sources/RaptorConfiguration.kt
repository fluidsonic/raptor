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
		fun configurationList(): RaptorConfiguration
		fun stringList(): List<String>
		fun string(): String
	}
}


operator fun RaptorConfiguration.get(path: String): RaptorConfiguration.Value? =
	valueOrNull(path)


fun RaptorConfiguration.value(path: String) =
	valueOrNull(path) ?: error("Configuration is missing a value for path '$path'.")
