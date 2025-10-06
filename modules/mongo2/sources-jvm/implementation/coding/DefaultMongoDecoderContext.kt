package io.fluidsonic.raptor.mongo2


internal data class DefaultMongoDecoderContext(
	override val bsonReader: MongoBsonReader,
	override val decoderRegistry: MongoDecoderRegistry,
) : MongoDecoderContext, MongoDecoderScope {

	override val context: MongoDecoderContext
		get() = this


	override fun asScope(): MongoDecoderScope =
		this
}
