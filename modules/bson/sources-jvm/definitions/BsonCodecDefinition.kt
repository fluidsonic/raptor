package io.fluidsonic.raptor.bson.internal

import io.fluidsonic.raptor.*
import kotlin.reflect.*
import org.bson.codecs.*


internal class BsonCodecDefinition<Value : Any>(
	private val codec: Codec<Value>,
) : RaptorBsonCodec<Value>, RaptorBsonDefinition {

	override val valueClass: KClass<Value> = codec.encoderClass.kotlin


	@Suppress("UNCHECKED_CAST")
	override fun <Value : Any> codecForValueClass(valueClass: KClass<Value>, registry: RaptorBsonCodecRegistry) =
		takeIf { codec.encoderClass == valueClass.java }
			?.let { it as RaptorBsonCodec<Value> }


	override fun RaptorBsonReaderScope.decode(arguments: List<KTypeProjection>?): Value =
		if (codec is DefaultScopedBsonCodec<Value>)
			codec.decode(reader.internal(), defaultDecoderContext, arguments = arguments)
		else
			codec.decode(reader.internal(), defaultDecoderContext)


	override fun RaptorBsonWriterScope.encode(value: Value) {
		codec.encode(writer.internal(), value, defaultEncoderContext)
	}


	companion object {

		private val defaultDecoderContext: DecoderContext = DecoderContext.builder().build()
		private val defaultEncoderContext: EncoderContext = EncoderContext.builder().build()
	}
}
