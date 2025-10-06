package io.fluidsonic.raptor.mongo2


internal data class RegistryListMongoDecoderRegistry(
	val registries: List<MongoDecoderRegistry>,
) : MongoDecoderRegistry by LookupMongoDecoderRegistry({ type ->
	registries.firstNotNullOfOrNull { it.findOrNull(type) }
}) {

	init {
		require(registries.isNotEmpty()) { "`registries` must not be empty." }
	}
}
