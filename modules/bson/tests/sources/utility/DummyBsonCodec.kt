package tests

import org.bson.*
import org.bson.codecs.*


object DummyBsonCodec : Codec<Any> {

	override fun getEncoderClass(): Class<Any> = Any::class.java
	override fun encode(writer: BsonWriter, value: Any?, encoderContext: EncoderContext) = TODO()
	override fun decode(reader: BsonReader, decoderContext: DecoderContext): Any = TODO()
}
