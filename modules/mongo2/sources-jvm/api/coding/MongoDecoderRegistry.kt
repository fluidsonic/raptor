package io.fluidsonic.raptor.mongo2


// Registries must cache their lookups.
public interface MongoDecoderRegistry {

	// FIXME in/out may change
	public fun <Value : Any> findOrNull(type: MongoValueType<Value>): MongoDecoder<Value>?


	public companion object {

		public fun empty(): MongoDecoderRegistry =
			EmptyMongoDecoderRegistry
	}
}


public fun <Value : Any> MongoDecoderRegistry.find(type: MongoValueType<Value>): MongoDecoder<Value> =
	findOrNull(type)
		?: error("No MongoDecoder found for type '$type'.")


public inline fun <reified Value : Any> MongoDecoderRegistry.find(): MongoDecoder<Value> =
	find(MongoValueType<Value>())


public inline fun <reified Value : Any> MongoDecoderRegistry.findOrNull(): MongoDecoder<Value>? =
	findOrNull(MongoValueType<Value>())


@JvmName("MongoDecoderRegistryOfDecoders")
public fun MongoDecoderRegistry(decoders: List<MongoDecoder<*>>): MongoDecoderRegistry =
	when {
		decoders.isEmpty() -> MongoDecoderRegistry.empty()
		else -> DecoderListMongoDecoderRegistry(decoders)
	}


@JvmName("MongoDecoderRegistryOfRegistries")
public fun MongoDecoderRegistry(registries: List<MongoDecoderRegistry>): MongoDecoderRegistry =
	when {
		registries.isEmpty() -> MongoDecoderRegistry.empty()
		else -> RegistryListMongoDecoderRegistry(registries)
	}


@JvmName("MongoDecoderRegistryOfDecoders")
public fun MongoDecoderRegistry(vararg decoders: MongoDecoder<*>): MongoDecoderRegistry =
	MongoDecoderRegistry(decoders.toList())


@JvmName("MongoDecoderRegistryOfRegistries")
public fun MongoDecoderRegistry(vararg registries: MongoDecoderRegistry): MongoDecoderRegistry =
	MongoDecoderRegistry(registries.toList())


@JvmName("plusDecoder")
public operator fun MongoDecoderRegistry.plus(other: MongoDecoder<*>): MongoDecoderRegistry =
	this + MongoDecoderRegistry(other)


@JvmName("plusDecoderList")
public operator fun MongoDecoderRegistry.plus(other: List<MongoDecoder<*>>): MongoDecoderRegistry =
	this + MongoDecoderRegistry(other)


public operator fun MongoDecoderRegistry.plus(other: MongoDecoderRegistry): MongoDecoderRegistry =
	MongoDecoderRegistry(this, other)


@JvmName("plusList")
public operator fun MongoDecoderRegistry.plus(other: List<MongoDecoderRegistry>): MongoDecoderRegistry =
	MongoDecoderRegistry(listOf(this) + other)


@JvmName("plusFromDecoder")
public operator fun MongoDecoder<*>.plus(self: MongoDecoderRegistry): MongoDecoderRegistry =
	MongoDecoderRegistry(this) + self


@JvmName("plusFromDecoderList")
public operator fun List<MongoDecoder<*>>.plus(self: MongoDecoderRegistry): MongoDecoderRegistry =
	MongoDecoderRegistry(this) + self


@JvmName("plusFromList")
public operator fun List<MongoDecoderRegistry>.plus(self: MongoDecoderRegistry): MongoDecoderRegistry =
	MongoDecoderRegistry(this + listOf(self))
