package io.fluidsonic.raptor

import org.bson.codecs.*
import org.bson.codecs.configuration.*
import kotlin.reflect.*


private class ScopedBsonDefinition<Value : Any>(
	private val encodesSubclasses: Boolean,
	private val config: RaptorBsonDefinitionScope<Value>.() -> Unit,
	override val valueClass: KClass<Value>
) : RaptorBsonDefinition<Value> {

	override fun codec(scope: BsonScope) =
		if (encodesSubclasses)
			null
		else
			createCodec(scope = scope)


	private fun createCodec(scope: BsonScope) =
		DefaultBsonDefinitionScope(scope = scope, valueClass = valueClass).apply(config).codec()


	override fun provider(scope: BsonScope): CodecProvider? {
		if (!encodesSubclasses)
			return null

		return Provider(codec = createCodec(scope = scope))
	}


	private class Provider<Value : Any>(
		private val codec: Codec<Value>
	) : CodecProvider {

		override fun <T : Any?> get(clazz: Class<T>, registry: CodecRegistry): Codec<T>? {
			@Suppress("UNCHECKED_CAST")
			if (codec.encoderClass.isAssignableFrom(clazz))
				return codec as Codec<T>

			return null
		}
	}
}


// FIXME rn?
// FIXME DslMarker
// FIXME add @BuilderInference once compiler errors are fixed
// FIXME add support for nested codecs
@RaptorDsl
inline fun <reified Value : Any> bsonDefinition(
	encodesSubclasses: Boolean = false,
	noinline config: RaptorBsonDefinitionScope<Value>.() -> Unit
) =
	bsonDefinition(valueClass = Value::class, encodesSubclasses = encodesSubclasses, config = config)


@RaptorDsl
fun <Value : Any> bsonDefinition(
	valueClass: KClass<Value>,
	encodesSubclasses: Boolean = false,
	config: RaptorBsonDefinitionScope<Value>.() -> Unit
): RaptorBsonDefinition<Value> =
	ScopedBsonDefinition(encodesSubclasses = encodesSubclasses, config = config, valueClass = valueClass)
