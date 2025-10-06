package io.fluidsonic.raptor.mongo2


public data class MongoCoderRegistry(
	public val decoder: MongoDecoderRegistry,
	public val encoder: MongoEncoderRegistry,
) {

	public companion object {

		private val empty: MongoCoderRegistry = MongoCoderRegistry(
			decoder = MongoDecoderRegistry.empty(),
			encoder = MongoEncoderRegistry.empty(),
		)


		public fun empty(): MongoCoderRegistry = empty
	}
}


@JvmName("plusDecoder")
public operator fun MongoCoderRegistry.plus(other: MongoDecoder<*>): MongoCoderRegistry =
	copy(decoder = this.decoder + other)


@JvmName("plusDecoderRegistry")
public operator fun MongoCoderRegistry.plus(other: MongoDecoderRegistry): MongoCoderRegistry =
	copy(decoder = this.decoder + other)


@JvmName("plusEncoder")
public operator fun MongoCoderRegistry.plus(other: MongoEncoder<*>): MongoCoderRegistry =
	copy(encoder = this.encoder + other)


@JvmName("plusEncoderRegistry")
public operator fun MongoCoderRegistry.plus(other: MongoEncoderRegistry): MongoCoderRegistry =
	copy(encoder = this.encoder + other)


@JvmName("plusCoder")
public operator fun <Coder> MongoCoderRegistry.plus(other: Coder): MongoCoderRegistry
	where Coder : MongoDecoder<*>, Coder : MongoEncoder<*> =
	MongoCoderRegistry(
		decoder = this.decoder + other,
		encoder = this.encoder + other,
	)


@JvmName("plusCoderRegistry")
public operator fun <Coder> MongoCoderRegistry.plus(other: Coder): MongoCoderRegistry
	where Coder : MongoDecoderRegistry, Coder : MongoEncoderRegistry =
	MongoCoderRegistry(
		decoder = this.decoder + other,
		encoder = this.encoder + other,
	)


public operator fun MongoCoderRegistry.plus(other: MongoCoderRegistry): MongoCoderRegistry =
	MongoCoderRegistry(
		decoder = this.decoder + other.decoder,
		encoder = this.encoder + other.encoder,
	)


@JvmName("plusDecoderList")
public operator fun MongoCoderRegistry.plus(other: List<MongoDecoder<*>>): MongoCoderRegistry =
	copy(decoder = this.decoder + other)


@JvmName("plusDecoderRegistryList")
public operator fun MongoCoderRegistry.plus(other: List<MongoDecoderRegistry>): MongoCoderRegistry =
	copy(decoder = this.decoder + other)


@JvmName("plusEncoderList")
public operator fun MongoCoderRegistry.plus(other: List<MongoEncoder<*>>): MongoCoderRegistry =
	copy(encoder = this.encoder + other)


@JvmName("plusEncoderRegistryList")
public operator fun MongoCoderRegistry.plus(other: List<MongoEncoderRegistry>): MongoCoderRegistry =
	copy(encoder = this.encoder + other)


@JvmName("plusCoderList")
public operator fun <Coder> MongoCoderRegistry.plus(other: List<Coder>): MongoCoderRegistry
	where Coder : MongoDecoder<*>, Coder : MongoEncoder<*> =
	MongoCoderRegistry(
		decoder = this.decoder + other,
		encoder = this.encoder + other,
	)


@JvmName("plusCoderRegistryList")
public operator fun <Coder> MongoCoderRegistry.plus(other: List<Coder>): MongoCoderRegistry
	where Coder : MongoDecoderRegistry, Coder : MongoEncoderRegistry =
	MongoCoderRegistry(
		decoder = this.decoder + other,
		encoder = this.encoder + other,
	)


@JvmName("plusList")
public operator fun MongoCoderRegistry.plus(other: List<MongoCoderRegistry>): MongoCoderRegistry =
	MongoCoderRegistry(
		decoder = this.decoder + other.map { it.decoder },
		encoder = this.encoder + other.map { it.encoder },
	)


@JvmName("plusFromDecoder")
public operator fun MongoDecoder<*>.plus(self: MongoCoderRegistry): MongoCoderRegistry =
	self.copy(decoder = this + self.decoder)


@JvmName("plusFromDecoderRegistry")
public operator fun MongoDecoderRegistry.plus(self: MongoCoderRegistry): MongoCoderRegistry =
	self.copy(decoder = this + self.decoder)


@JvmName("plusFromEncoder")
public operator fun MongoEncoder<*>.plus(self: MongoCoderRegistry): MongoCoderRegistry =
	self.copy(encoder = this + self.encoder)


@JvmName("plusFromEncoderRegistry")
public operator fun MongoEncoderRegistry.plus(self: MongoCoderRegistry): MongoCoderRegistry =
	self.copy(encoder = this + self.encoder)


@JvmName("plusFromCoder")
public operator fun <Coder> Coder.plus(self: MongoCoderRegistry): MongoCoderRegistry
	where Coder : MongoDecoder<*>, Coder : MongoEncoder<*> =
	MongoCoderRegistry(
		decoder = this + self.decoder,
		encoder = this + self.encoder,
	)


@JvmName("plusFromCoderRegistry")
public operator fun <Coder> Coder.plus(self: MongoCoderRegistry): MongoCoderRegistry
	where Coder : MongoDecoderRegistry, Coder : MongoEncoderRegistry =
	MongoCoderRegistry(
		decoder = this + self.decoder,
		encoder = this + self.encoder,
	)


@JvmName("plusFromDecoderList")
public operator fun List<MongoDecoder<*>>.plus(self: MongoCoderRegistry): MongoCoderRegistry =
	self.copy(decoder = this + self.decoder)


@JvmName("plusFromDecoderRegistryList")
public operator fun List<MongoDecoderRegistry>.plus(self: MongoCoderRegistry): MongoCoderRegistry =
	self.copy(decoder = this + self.decoder)


@JvmName("plusFromEncoderList")
public operator fun List<MongoEncoder<*>>.plus(self: MongoCoderRegistry): MongoCoderRegistry =
	self.copy(encoder = this + self.encoder)


@JvmName("plusFromEncoderRegistryList")
public operator fun List<MongoEncoderRegistry>.plus(self: MongoCoderRegistry): MongoCoderRegistry =
	self.copy(encoder = this + self.encoder)


@JvmName("plusFromCoderList")
public operator fun <Coder> List<Coder>.plus(self: MongoCoderRegistry): MongoCoderRegistry
	where Coder : MongoDecoder<*>, Coder : MongoEncoder<*> =
	MongoCoderRegistry(
		decoder = this + self.decoder,
		encoder = this + self.encoder,
	)


@JvmName("plusFromCoderRegistryList")
public operator fun <Coder> List<Coder>.plus(self: MongoCoderRegistry): MongoCoderRegistry
	where Coder : MongoDecoderRegistry, Coder : MongoEncoderRegistry =
	MongoCoderRegistry(
		decoder = this + self.decoder,
		encoder = this + self.encoder,
	)


@JvmName("plusFromList")
public operator fun List<MongoCoderRegistry>.plus(self: MongoCoderRegistry): MongoCoderRegistry =
	MongoCoderRegistry(
		decoder = this.map { it.decoder } + self.decoder,
		encoder = this.map { it.encoder } + self.encoder,
	)
