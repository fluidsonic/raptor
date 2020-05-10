package tests

import org.bson.codecs.*
import org.bson.codecs.configuration.*


object DummyBsonCodecProvider : CodecProvider {

	override fun <T : Any> get(clazz: Class<T>, registry: CodecRegistry): Codec<T> = TODO()
}
