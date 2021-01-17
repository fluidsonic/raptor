package io.fluidsonic.raptor

import io.fluidsonic.mongo.*
import kotlin.reflect.*
import org.bson.*
import org.bson.codecs.*
import org.bson.codecs.configuration.*


@OptIn(ExperimentalStdlibApi::class)
public inline fun <reified TDocument : Any> MongoDatabase.getCollectionOfGeneric(name: String): MongoCollection<TDocument> =
	getCollectionOfGeneric(name = name, type = typeOf<TDocument>())


@Suppress("UNCHECKED_CAST")
public fun <TDocument : Any> MongoDatabase.getCollectionOfGeneric(name: String, type: KType): MongoCollection<TDocument> {
	val documentClass = type.classifier as KClass<*> as KClass<TDocument>
	val arguments = type.arguments

	return when {
		arguments.isEmpty() -> getCollection(name, documentClass = documentClass)
		else -> getCollection(name = name, documentClass = GenericBsonCodec::class)
			.withCodecRegistry(CodecRegistries.fromRegistries(
				CodecRegistries.fromCodecs(GenericBsonCodec(
					documentCodec = codecRegistry.get(documentClass.java)
						.let { codec ->
							codec as? CodecEx<TDocument> ?: error("Codec ${codec::class} does not support decoding generic type $type.")
						},
					arguments = arguments,
				)),
				codecRegistry,
			))
			.let { it as MongoCollection<TDocument> }
	}
}


private class GenericBsonCodec<TDocument : Any>(
	private val documentCodec: CodecEx<TDocument>,
	private val arguments: List<KTypeProjection>,
) : Codec<TDocument> {

	override fun encode(writer: BsonWriter, value: TDocument, encoderContext: EncoderContext) {
		error("Cannot encode value: ${value::class}")
	}


	@Suppress("UNCHECKED_CAST")
	override fun getEncoderClass(): Class<TDocument> =
		GenericBsonCodec::class.java as Class<TDocument>


	override fun decode(reader: BsonReader, decoderContext: DecoderContext): TDocument =
		documentCodec.decode(reader = reader, decoderContext = decoderContext, arguments = arguments)
}
