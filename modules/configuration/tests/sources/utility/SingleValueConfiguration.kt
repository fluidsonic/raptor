package tests

import io.fluidsonic.raptor.*


class SingleValueConfiguration(
	private val path: String,
	value: String
) : RaptorConfiguration {

	private val value = Value(value)


	override fun hasValue(path: String) =
		path == this.path


	override fun valueOrNull(path: String): RaptorConfiguration.Value? =
		value.takeIf { path == this.path }


	private class Value(private val string: String) : RaptorConfiguration.Value {

		override fun configuration(): RaptorConfiguration =
			TODO()


		override fun configurationList(): List<RaptorConfiguration> =
			TODO()


		override fun stringList(): List<String> =
			TODO()


		override fun string() =
			string
	}
}
