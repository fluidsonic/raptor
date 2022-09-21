package io.fluidsonic.raptor.bson

import io.fluidsonic.stdlib.*
import java.util.concurrent.*
import kotlin.reflect.*
import org.bson.*
import org.bson.codecs.*
import org.bson.codecs.configuration.*


internal class DefaultBsonRootCodecRegistry(
	private val definitions: List<RaptorBsonDefinition>,
	private val scope: RaptorBsonScope,
) : RaptorBsonCodecRegistry, CodecProvider {

	private val externalCache: MutableMap<Class<*>, Codec<*>> = ConcurrentHashMap()
	private val internalCache: MutableMap<KClass<*>, RaptorBsonCodec<*>> = ConcurrentHashMap()
	private val registry: CodecRegistry = CodecRegistries.fromProviders(this)


	@Suppress("UNCHECKED_CAST")
	override fun <Value : Any> get(valueClass: Class<Value>, registry: CodecRegistry): Codec<Value>? =
		externalCache.getOrPut(valueClass) {
			getOrNull(valueClass.kotlin, registry = registry)
				?.let { codec ->
					when {
						codec is Codec<*> && codec.encoderClass == valueClass -> codec
						else -> DefaultScopedBsonCodec(codec = codec, scope = scope)
					}
				}
				.ifNull { NullCodec }
		}
			.takeIf { it !== NullCodec }
			?.let { it as Codec<Value> }


	override fun <Value : Any> getOrNull(valueClass: KClass<Value>): RaptorBsonCodec<Value>? =
		getOrNull(valueClass, registry = registry)


	@Suppress("UNCHECKED_CAST")
	private fun <Value : Any> getOrNull(valueClass: KClass<Value>, registry: CodecRegistry): RaptorBsonCodec<Value>? =
		internalCache
			.getOrPut(valueClass) {
				for (definition in definitions) {
					when (definition) {
						is BsonCodecProviderDefinition ->
							definition.codecProvider.get(valueClass.javaObjectType, registry)
								?.let { return@getOrPut BsonCodecDefinition(it) }

						else ->
							definition.codecForValueClass(valueClass, registry = this)?.let { return@getOrPut it }
					}
				}

				NullCodec
			}
			.takeIf { it !== NullCodec }
			?.let { it as RaptorBsonCodec<Value> }


	override fun internal(): CodecRegistry =
		registry


	override fun toString() = buildString {
		if (definitions.isEmpty())
			append("Empty ")

		append("Raptor BSON registry")

		if (definitions.isNotEmpty()) {
			append(":\n\t")
			definitions.joinTo(this, separator = "\n\t")
		}
	}


	private object NullCodec : Codec<Nothing>, RaptorBsonCodec<Nothing> {

		override val valueClass: KClass<Nothing>
			get() = error("Not possible.")


		override fun decode(reader: BsonReader, decoderContext: DecoderContext): Nothing =
			error("Not possible.")


		override fun encode(writer: BsonWriter, value: Nothing, encoderContext: EncoderContext) =
			error("Not possible.")


		override fun getEncoderClass(): Class<Nothing> =
			error("Not possible.")


		override fun RaptorBsonReaderScope.decode(arguments: List<KTypeProjection>?): Nothing =
			error("Not possible.")


		override fun RaptorBsonWriterScope.encode(value: Nothing) =
			error("Not possible.")
	}
}
