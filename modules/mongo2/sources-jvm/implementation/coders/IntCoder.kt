package io.fluidsonic.raptor.mongo2


internal object IntCoder : MongoCoder<Int> {

	override fun decodes(type: MongoValueType<in Int>) =
		type.classifier == Int::class


	override fun encodes(type: MongoValueType<out Int>) =
		type.classifier == Int::class


	override fun MongoDecoderScope.decode(type: MongoValueType<in Int>): Int =
		int()


	override fun MongoEncoderScope.encode(value: Int, type: MongoValueType<out Int>) {
		int(value)
	}
}
