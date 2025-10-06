package io.fluidsonic.raptor.mongo2


// Registries must cache their lookups.
public interface MongoEncoderRegistry {

	public fun <Value : Any> findOrNull(type: MongoValueType<Value>): MongoEncoder<Value>?


	public companion object {

		public fun empty(): MongoEncoderRegistry =
			EmptyMongoEncoderRegistry
	}
}


public fun <Value : Any> MongoEncoderRegistry.find(type: MongoValueType<Value>): MongoEncoder<Value> =
	findOrNull(type)
		?: error("No MongoEncoder found for type '$type'.")


public inline fun <reified Value : Any> MongoEncoderRegistry.find(): MongoEncoder<Value> =
	find(MongoValueType<Value>())


public inline fun <reified Value : Any> MongoEncoderRegistry.findOrNull(): MongoEncoder<Value>? =
	findOrNull(MongoValueType<Value>())


@JvmName("MongoEncoderRegistryOfEncoders")
public fun MongoEncoderRegistry(encoders: List<MongoEncoder<*>>): MongoEncoderRegistry =
	when {
		encoders.isEmpty() -> MongoEncoderRegistry.empty()
		else -> EncoderListMongoEncoderRegistry(encoders)
	}


@JvmName("MongoEncoderRegistryOfRegistries")
public fun MongoEncoderRegistry(registries: List<MongoEncoderRegistry>): MongoEncoderRegistry =
	when {
		registries.isEmpty() -> MongoEncoderRegistry.empty()
		else -> RegistryListMongoEncoderRegistry(registries)
	}


@JvmName("MongoEncoderRegistryOfEncoders")
public fun MongoEncoderRegistry(vararg encoders: MongoEncoder<*>): MongoEncoderRegistry =
	MongoEncoderRegistry(encoders.toList())


@JvmName("MongoEncoderRegistryOfRegistries")
public fun MongoEncoderRegistry(vararg registries: MongoEncoderRegistry): MongoEncoderRegistry =
	MongoEncoderRegistry(registries.toList())


@JvmName("plusEncoder")
public operator fun MongoEncoderRegistry.plus(other: MongoEncoder<*>): MongoEncoderRegistry =
	this + MongoEncoderRegistry(other)


@JvmName("plusEncoderList")
public operator fun MongoEncoderRegistry.plus(other: List<MongoEncoder<*>>): MongoEncoderRegistry =
	this + MongoEncoderRegistry(other)


public operator fun MongoEncoderRegistry.plus(other: MongoEncoderRegistry): MongoEncoderRegistry =
	MongoEncoderRegistry(this, other)


@JvmName("plusList")
public operator fun MongoEncoderRegistry.plus(other: List<MongoEncoderRegistry>): MongoEncoderRegistry =
	MongoEncoderRegistry(listOf(this) + other)


@JvmName("plusFromEncoder")
public operator fun MongoEncoder<*>.plus(self: MongoEncoderRegistry): MongoEncoderRegistry =
	MongoEncoderRegistry(this) + self


@JvmName("plusFromEncoderList")
public operator fun List<MongoEncoder<*>>.plus(self: MongoEncoderRegistry): MongoEncoderRegistry =
	MongoEncoderRegistry(this) + self


@JvmName("plusFromList")
public operator fun List<MongoEncoderRegistry>.plus(self: MongoEncoderRegistry): MongoEncoderRegistry =
	MongoEncoderRegistry(this + listOf(self))
