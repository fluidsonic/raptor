package io.fluidsonic.raptor

import org.bson.codecs.*
import org.bson.codecs.configuration.*
import kotlin.reflect.*


private class ScopedBsonDefinition<Value : Any>(
	private val encodesSubclasses: Boolean,
	private val config: RaptorBsonDefinitionScope<Value>.() -> Unit,
	private val valueClass: KClass<Value>,
) : RaptorBsonDefinitions {

	override fun createCodecRegistry(scope: BsonScope): CodecRegistry =
		CodecRegistries.fromProviders(Provider(
			codec = DefaultBsonDefinitionScope(scope = scope, valueClass = valueClass).apply(config).codec(),
			encodesSubclasses = encodesSubclasses
		))


	override fun toString() =
		"<BSON definition: ${valueClass.qualifiedName}>"


	private class Provider<Value : Any>(
		private val codec: Codec<Value>,
		private val encodesSubclasses: Boolean,
	) : CodecProvider {

		@Suppress("UNCHECKED_CAST")
		override fun <T : Any?> get(clazz: Class<T>, registry: CodecRegistry): Codec<T>? {
			when (encodesSubclasses) {
				true ->
					if (codec.encoderClass.isAssignableFrom(clazz))
						return codec as Codec<T>

				false ->
					if (clazz == codec.encoderClass)
						return codec as Codec<T>
			}

			return null
		}
	}
}


// FIXME rn?
// FIXME DslMarker
// FIXME add @BuilderInference once compiler errors are fixed
// FIXME add support for nested codecs
@RaptorDsl
public inline fun <reified Value : Any> bsonDefinition(
	encodesSubclasses: Boolean = false,
	noinline config: RaptorBsonDefinitionScope<Value>.() -> Unit,
): RaptorBsonDefinitions =
	bsonDefinition(valueClass = Value::class, encodesSubclasses = encodesSubclasses, config = config)


@RaptorDsl
public fun <Value : Any> bsonDefinition(
	valueClass: KClass<Value>,
	encodesSubclasses: Boolean = false,
	config: RaptorBsonDefinitionScope<Value>.() -> Unit,
): RaptorBsonDefinitions =
	ScopedBsonDefinition(encodesSubclasses = encodesSubclasses, config = config, valueClass = valueClass)
