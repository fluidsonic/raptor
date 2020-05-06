package io.fluidsonic.raptor

import org.bson.codecs.*
import org.bson.codecs.configuration.*


@Raptor.Dsl3
class BsonRaptorComponent internal constructor() : RaptorComponent {

	private val codecs: MutableList<Codec<*>> = mutableListOf()
	private val definitions: MutableList<RaptorBsonDefinition<*>> = mutableListOf()
	private val providers: MutableList<CodecProvider> = mutableListOf()
	private val registries: MutableList<CodecRegistry> = mutableListOf()


	internal fun complete() = BsonConfig(
		codecs = codecs,
		definitions = definitions,
		providers = providers,
		registries = registries
	)


	@Raptor.Dsl3
	fun codecs(vararg codecs: Codec<*>) {
		codecs(codecs.asIterable())
	}


	@Raptor.Dsl3
	fun codecs(codecs: Iterable<Codec<*>>) {
		this.codecs += codecs
	}


	@Raptor.Dsl3
	fun definitions(vararg definitions: RaptorBsonDefinition<*>) {
		definitions(definitions.asIterable())
	}


	@Raptor.Dsl3
	fun definitions(definitions: Iterable<RaptorBsonDefinition<*>>) {
		this.definitions += definitions
	}


	@Raptor.Dsl3
	fun providers(vararg providers: CodecProvider) {
		providers(providers.asIterable())
	}


	@Raptor.Dsl3
	fun providers(providers: Iterable<CodecProvider>) {
		this.providers += providers
	}


	@Raptor.Dsl3
	fun registries(vararg registries: CodecRegistry) {
		registries(registries.asIterable())
	}


	@Raptor.Dsl3
	fun registries(registries: Iterable<CodecRegistry>) {
		this.registries += registries
	}
}
