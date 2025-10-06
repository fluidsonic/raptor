package io.fluidsonic.raptor.mongo2


internal object StringCoder : MongoCoder<String> {

	override fun decodes(type: MongoValueType<in String>) =
		type.classifier == String::class


	override fun encodes(type: MongoValueType<out String>) =
		type.classifier == String::class


	override fun MongoDecoderScope.decode(type: MongoValueType<in String>) =
		string()


	override fun MongoEncoderScope.encode(value: String, type: MongoValueType<out String>) {
		string(value)
	}
}
