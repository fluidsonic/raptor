package io.fluidsonic.raptor.mongo2


internal data class DefaultMongoEncoderContext(
	override val bsonWriter: MongoBsonWriter,
	override val encoderRegistry: MongoEncoderRegistry,
) : MongoEncoderContext, MongoEncoderScope {

	override val context: MongoEncoderContext
		get() = this


	override fun asScope(): MongoEncoderScope =
		this
}
