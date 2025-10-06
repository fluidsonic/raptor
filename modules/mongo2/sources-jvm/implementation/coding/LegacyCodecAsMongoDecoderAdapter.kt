package io.fluidsonic.raptor.mongo2

import org.bson.codecs.*
import org.bson.codecs.DecoderContext as SourceContext


internal data class LegacyCodecAsMongoDecoderAdapter<Value : Any>(
	private val codec: Codec<Value>,
) : MongoDecoder<Value> {

	override fun decodes(type: MongoValueType<in Value>) =
		type.classifier.java == codec.encoderClass && type.arguments.isEmpty() // FIXME


	override fun MongoDecoderScope.decode(type: MongoValueType<in Value>): Value =
		codec.decode(checkNotNull(bsonReader.asLegacy()), sourceContext) // FIXME


	companion object {

		private val sourceContext: SourceContext = SourceContext.builder().build()
	}
}
