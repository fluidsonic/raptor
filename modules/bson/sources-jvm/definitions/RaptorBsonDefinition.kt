package io.fluidsonic.raptor

import io.fluidsonic.raptor.bson.internal.*
import kotlin.reflect.*
import org.bson.codecs.*
import org.bson.codecs.configuration.*


public interface RaptorBsonDefinition {

	public fun <Value : Any> codecForValueClass(valueClass: KClass<Value>, registry: RaptorBsonCodecRegistry): RaptorBsonCodec<Value>?


	public companion object {

		@RaptorInternalApi
		public fun of(codec: Codec<*>): RaptorBsonDefinition =
			BsonCodecDefinition(codec)


		@RaptorInternalApi
		public fun of(provider: CodecProvider): RaptorBsonDefinition =
			BsonCodecProviderDefinition(provider)
	}


	public enum class Priority {

		high,
		normal,
		low
	}
}
