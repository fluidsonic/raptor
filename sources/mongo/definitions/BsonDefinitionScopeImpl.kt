package io.fluidsonic.raptor

import org.bson.*
import kotlin.reflect.*


internal class BsonDefinitionScopeImpl<Value : Any>(
	scope: RaptorBsonScope,
	val valueClass: KClass<Value>
) : RaptorBsonDefinitionScope<Value>, RaptorBsonScope by scope {

	private var decoder: (BsonReader.() -> Value)? = null
	private var encoder: (BsonWriter.(value: Value) -> Unit)? = null


	fun codec() = CallbackBsonCodec(
		decoder = decoder,
		encoder = encoder,
		valueClass = valueClass
	)


	override fun decode(decoder: BsonReader.() -> Value) {
		check(this.decoder == null) { "Cannot define multiple decoders." }

		this.decoder = decoder
	}


	override fun encode(encoder: BsonWriter.(value: Value) -> Unit) {
		check(this.encoder == null) { "Cannot define multiple encoder." }

		this.encoder = encoder
	}
}
