package io.fluidsonic.raptor

import org.bson.*
import org.bson.codecs.*
import kotlin.reflect.*


internal class CallbackBsonCodec<Value : Any>(
	private val decoder: (BsonReader.() -> Value)?,
	private val encoder: (BsonWriter.(value: Value) -> Unit)?,
	private val valueClass: KClass<Value>
) : Codec<Value> {

	override fun getEncoderClass() =
		valueClass.java


	override fun decode(reader: BsonReader, decoderContext: DecoderContext): Value =
		decoder?.invoke(reader) ?: error("BSON codec for ${valueClass.qualifiedName} doesn't support decoding.")


	override fun encode(writer: BsonWriter, value: Value, encoderContext: EncoderContext) {
		encoder?.invoke(writer, value) ?: error("BSON codec for ${valueClass.qualifiedName} doesn't support encoding.")
	}
}
