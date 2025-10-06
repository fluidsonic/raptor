package io.fluidsonic.raptor.mongo2


internal data class EncoderListMongoEncoderRegistry(
	private val encoders: List<MongoEncoder<*>>,
) : MongoEncoderRegistry by LookupMongoEncoderRegistry({ type ->
	encoders.firstOrNull { it.encodes(type as MongoValueType<Nothing>) } // FIXME
}) {

	init {
		require(encoders.isNotEmpty()) { "`encoders` must not be empty." }
	}
}
