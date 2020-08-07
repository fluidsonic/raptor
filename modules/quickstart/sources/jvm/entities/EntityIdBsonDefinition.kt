package io.fluidsonic.raptor.quickstart.internal

import io.fluidsonic.raptor.*
import org.bson.*
import org.bson.codecs.*
import org.bson.codecs.configuration.*


internal class EntityIdBsonDefinition<Id : EntityId>(
	val factory: EntityId.Factory<Id>,
) : RaptorBsonDefinitions, Codec<Id>, CodecRegistry {

	private val encoderClass = factory.idClass.java


	override fun createCodecRegistry(scope: BsonScope): CodecRegistry =
		this


	override fun decode(reader: BsonReader, decoderContext: DecoderContext): Id =
		with(factory) {
			reader.readIdValue()
		}


	override fun encode(writer: BsonWriter, value: Id, encoderContext: EncoderContext) {
		with(factory) {
			writer.writeIdValue(value)
		}
	}


	@Suppress("UNCHECKED_CAST")
	override fun <T : Any?> get(clazz: Class<T>): Codec<T>? =
		takeIf { clazz == encoderClass } as Codec<T>?


	override fun <T : Any?> get(clazz: Class<T>, registry: CodecRegistry) =
		get(clazz)


	override fun getEncoderClass() =
		encoderClass
}
