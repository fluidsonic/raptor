package tests

import io.fluidsonic.raptor.*


class SingleValueSettings(
	private val path: String,
	value: String
) : RaptorSettings {

	private val value = Value(value)


	override fun hasValue(path: String) =
		path == this.path


	override fun valueOrNull(path: String): RaptorSettings.Value? =
		value.takeIf { path == this.path }


	private class Value(private val string: String) : RaptorSettings.Value {

		override fun settings(): RaptorSettings =
			TODO()


		override fun settingsList(): List<RaptorSettings> =
			TODO()


		override fun stringList(): List<String> =
			TODO()


		override fun string() =
			string
	}
}
