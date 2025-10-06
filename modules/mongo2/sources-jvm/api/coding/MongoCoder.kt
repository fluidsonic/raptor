package io.fluidsonic.raptor.mongo2


public interface MongoCoder<Value : Any> : MongoDecoder<Value>, MongoEncoder<Value> {

	public companion object
}
