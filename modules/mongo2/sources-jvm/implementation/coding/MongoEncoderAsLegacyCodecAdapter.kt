package io.fluidsonic.raptor.mongo2

import org.bson.codecs.*
import org.bson.codecs.EncoderContext as SourceContext


internal data class MongoEncoderAsLegacyCodecAdapter<Value : Any>(
	private val codec: Codec<Value>,
) : MongoEncoder<Value> {

	override fun encodes(type: MongoValueType<out Value>) =
		type == codec.encoderClass && type.arguments.isEmpty() // FIXME


	override fun MongoEncoderScope.encode(value: Value, type: MongoValueType<out Value>) {
		codec.encode(checkNotNull(bsonWriter.asLegacy()), value, sourceContext) // FIXME
	}


	companion object {

		private val sourceContext: SourceContext = SourceContext.builder().build()
	}
}
