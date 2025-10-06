package io.fluidsonic.raptor.mongo2


public interface MongoEncoderContext {

	public val encoderRegistry: MongoEncoderRegistry

	public fun asScope(): MongoEncoderScope


	public companion object
}
