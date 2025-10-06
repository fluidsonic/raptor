package io.fluidsonic.raptor.mongo2


public interface MongoDecoderContext {

	public val decoderRegistry: MongoDecoderRegistry

	public fun asScope(): MongoDecoderScope


	public companion object
}
