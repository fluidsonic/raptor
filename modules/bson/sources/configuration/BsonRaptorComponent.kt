package io.fluidsonic.raptor

import org.bson.codecs.*
import org.bson.codecs.configuration.*


class BsonRaptorComponent internal constructor() : RaptorComponent.Default<BsonRaptorComponent>() {

	internal val codecs: MutableList<Codec<*>> = mutableListOf()
	internal val definitions: MutableList<RaptorBsonDefinition<*>> = mutableListOf()
	internal var includesDefaultCodecs = false
	internal val providers: MutableList<CodecProvider> = mutableListOf()
	internal val registries: MutableList<CodecRegistry> = mutableListOf()


	override fun RaptorComponentConfigurationEndScope.onConfigurationEnded() {
		propertyRegistry.register(BsonConfiguration.PropertyKey, BsonConfiguration(
			codecs = codecs,
			definitions = definitions,
			providers = providers,
			registries = registries
		))
	}


	companion object;


	internal object Key : RaptorComponentKey<BsonRaptorComponent> {

		override fun toString() = "bson"
	}
}


@RaptorDsl
fun RaptorComponentSet<BsonRaptorComponent>.codecs(vararg codecs: Codec<*>) {
	codecs(codecs.asIterable())
}


@RaptorDsl
fun RaptorComponentSet<BsonRaptorComponent>.codecs(codecs: Iterable<Codec<*>>) = configure {
	this.codecs += codecs
}


// FIXME can be confused with global bsonDefinition()
@RaptorDsl
fun RaptorComponentSet<BsonRaptorComponent>.definitions(vararg definitions: RaptorBsonDefinition<*>) {
	definitions(definitions.asIterable())
}


@RaptorDsl
fun RaptorComponentSet<BsonRaptorComponent>.definitions(definitions: Iterable<RaptorBsonDefinition<*>>) = configure {
	this.definitions += definitions
}


@RaptorDsl
fun RaptorComponentSet<BsonRaptorComponent>.includeDefaultCodecs() = configure {
	if (includesDefaultCodecs)
		return@configure

	includesDefaultCodecs = true

	definitions(RaptorBsonDefaults.definitions)
	providers(RaptorBsonDefaults.providers)
}


@RaptorDsl
fun RaptorComponentSet<BsonRaptorComponent>.providers(vararg providers: CodecProvider) {
	providers(providers.asIterable())
}


@RaptorDsl
fun RaptorComponentSet<BsonRaptorComponent>.providers(providers: Iterable<CodecProvider>) = configure {
	this.providers += providers
}


@RaptorDsl
fun RaptorComponentSet<BsonRaptorComponent>.registries(vararg registries: CodecRegistry) {
	registries(registries.asIterable())
}


@RaptorDsl
fun RaptorComponentSet<BsonRaptorComponent>.registries(registries: Iterable<CodecRegistry>) = configure {
	this.registries += registries
}
