package io.fluidsonic.raptor.mongo2

import java.lang.reflect.*
import java.util.concurrent.*
import org.bson.*
import org.bson.codecs.*
import org.bson.codecs.configuration.CodecRegistry as LegacyCodecRegistry


// FIXME
private data class MongoCoderRegistryAsLegacyCodecRegistryAdapter(
	private val coderRegistry: MongoCoderRegistry,
) : LegacyCodecRegistry {

	private val cache = ConcurrentHashMap<Class<*>, Codec<*>>()


	@Suppress("UNCHECKED_CAST")
	override fun <Value : Any> get(valueClass: Class<Value>): Codec<Value> =
		cache.getOrPut(valueClass) {
			legacyCodec(MongoValueType(valueClass.kotlin))
		} as Codec<Value>


	override fun <Value : Any> get(valueClass: Class<Value>, registry: LegacyCodecRegistry): Codec<Value> =
		get(valueClass)


	override fun <Value : Any> get(valueClass: Class<Value>, typeArguments: List<Type>): Codec<Value> =
		get(valueClass)


	override fun <Value : Any> get(valueClass: Class<Value>, typeArguments: List<Type>, registry: LegacyCodecRegistry): Codec<Value> =
		get(valueClass, typeArguments)


	private fun <Value : Any> legacyCodec(valueType: MongoValueType<Value>): Codec<Value> =
		CoderLegacyCodec(coderRegistry = coderRegistry, valueType = valueType)


	private object NotFoundCodec : Codec<Nothing> {

		override fun decode(reader: BsonReader, decoderContext: DecoderContext): Nothing =
			error("This is just a marker that no codec was found.")


		override fun encode(writer: BsonWriter, value: Nothing?, encoderContext: EncoderContext) =
			error("This is just a marker that no codec was found.")


		override fun getEncoderClass(): Class<Nothing> =
			Nothing::class.java
	}
}


internal fun MongoCoderRegistry.asLegacy(): LegacyCodecRegistry =
	MongoCoderRegistryAsLegacyCodecRegistryAdapter(coderRegistry = this)
