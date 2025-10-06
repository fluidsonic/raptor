package io.fluidsonic.raptor.mongo2


internal data class DecoderListMongoDecoderRegistry(
	private val decoders: List<MongoDecoder<*>>,
) : MongoDecoderRegistry by LookupMongoDecoderRegistry({ type ->
	decoders.firstOrNull { it.decodes(type as MongoValueType<in Any>) } // FIXME
}) {

	init {
		require(decoders.isNotEmpty()) { "`decoders` must not be empty." }
	}
}
