package io.fluidsonic.raptor.mongo2

import org.bson.*
import org.bson.codecs.*


internal class CoderLegacyCodec<Value : Any>(
	private val coderRegistry: MongoCoderRegistry,
	private val valueType: MongoValueType<Value>,
) : Codec<Value> {

	private val decoder by lazy { coderRegistry.decoder.find(valueType) }
	private val encoder by lazy { coderRegistry.encoder.find(valueType) }


	override fun decode(reader: BsonReader, decoderContext: DecoderContext): Value =
		with(decoder) {
			DefaultMongoDecoderContext(LegacyMongoBsonReader(reader), coderRegistry.decoder).decode(valueType)
		}


	override fun encode(writer: BsonWriter, value: Value, encoderContext: EncoderContext) {
		with(encoder) {
			DefaultMongoEncoderContext(LegacyMongoBsonWriter(writer), coderRegistry.encoder).encode(value, valueType)
		}
	}


	override fun getEncoderClass(): Class<Value> =
		valueType.classifier.java
}
