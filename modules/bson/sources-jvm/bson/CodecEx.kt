package io.fluidsonic.raptor.bson

import kotlin.reflect.*
import org.bson.*
import org.bson.codecs.*


// FIXME rn
public interface CodecEx<TDocument : Any> : Codec<TDocument> {

	override fun decode(reader: BsonReader, decoderContext: DecoderContext): TDocument =
		decode(reader = reader, decoderContext = decoderContext, arguments = null)


	public fun decode(reader: BsonReader, decoderContext: DecoderContext, arguments: List<KTypeProjection>?): TDocument
}
