package io.fluidsonic.raptor.mongo2


internal data class RegistryListMongoEncoderRegistry(
	val registries: List<MongoEncoderRegistry>,
) : MongoEncoderRegistry by LookupMongoEncoderRegistry({ type ->
	registries.firstNotNullOfOrNull { it.findOrNull(type) }
}) {

	init {
		require(registries.isNotEmpty()) { "`registries` must not be empty." }
	}
}
