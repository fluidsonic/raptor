package io.fluidsonic.raptor

import kotlin.reflect.*
import org.bson.codecs.configuration.*


public interface RaptorBsonCodecRegistry {

	@RaptorInternalApi
	public fun internal(): CodecRegistry


	public operator fun <Value : Any> get(valueClass: KClass<Value>): RaptorBsonCodec<Value> =
		getOrNull(valueClass)
			?: error(
				"No BSON definition was provided for type '${valueClass.qualifiedName}'.\n" +
					"Add a `raptor.bson.definition { … }` for that type and register it in a RaptorFeature with `bson.definitions(…)`."
			)


	public fun <Value : Any> getOrNull(valueClass: KClass<Value>): RaptorBsonCodec<Value>?
}


@Suppress("UNCHECKED_CAST")
public fun <Value : Any> RaptorBsonCodecRegistry.decode(
	scope: RaptorBsonReaderScope,
	valueClass: KClass<Value>,
	arguments: List<KTypeProjection>,
): Value =
	this[valueClass].decode(scope = scope, arguments = arguments)


@Suppress("UNCHECKED_CAST")
public fun <Value : Any> RaptorBsonCodecRegistry.encode(scope: RaptorBsonWriterScope, value: Value) {
	this[value::class]
		.let { it as RaptorBsonCodec<Value> }
		.encode(scope = scope, value = value)
}
