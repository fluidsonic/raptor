package io.fluidsonic.raptor.bson

import kotlin.reflect.*
import org.bson.*
import org.bson.codecs.*


internal class DefaultScopedBsonCodec<Value : Any>(
	private val codec: RaptorBsonCodec<Value>,
	private val scope: RaptorBsonScope,
) : CodecEx<Value> {

	override fun decode(reader: BsonReader, decoderContext: DecoderContext, arguments: List<KTypeProjection>?): Value =
		with(codec) {
			reader.asScope().decode(arguments)
		}


	override fun encode(writer: BsonWriter, value: Value, encoderContext: EncoderContext) {
		with(codec) {
			writer.asScope().encode(value)
		}
	}


	override fun getEncoderClass(): Class<Value> =
		codec.valueClass.java


	private fun BsonReader.asScope(): RaptorBsonReaderScope =
		(this as? DefaultBsonReaderScope) ?: DefaultBsonReaderScope(parent = scope, reader = this)


	private fun BsonWriter.asScope(): RaptorBsonWriterScope =
		(this as? DefaultBsonWriterScope) ?: DefaultBsonWriterScope(parent = scope, writer = this)
}
