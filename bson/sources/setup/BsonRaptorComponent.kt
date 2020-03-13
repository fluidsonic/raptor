package io.fluidsonic.raptor

import org.bson.codecs.*
import org.bson.codecs.configuration.*


@Raptor.Dsl3
class BsonRaptorComponent internal constructor() : RaptorComponent {

	internal val codecs: MutableList<Codec<*>> = mutableListOf()
	internal val definitions: MutableList<RaptorBsonDefinition<*>> = mutableListOf()
	internal val providers: MutableList<CodecProvider> = mutableListOf()
	internal val registries: MutableList<CodecRegistry> = mutableListOf()
}


@Raptor.Dsl3
fun RaptorComponentScope<BsonRaptorComponent>.codecs(vararg codecs: Codec<*>) {
	codecs(codecs.asIterable())
}


@Raptor.Dsl3
fun RaptorComponentScope<BsonRaptorComponent>.codecs(codecs: Iterable<Codec<*>>) {
	raptorComponentSelection {
		component.codecs += codecs
	}
}


@Raptor.Dsl3
fun RaptorComponentScope<BsonRaptorComponent>.definitions(vararg definitions: RaptorBsonDefinition<*>) {
	definitions(definitions.asIterable())
}


@Raptor.Dsl3
fun RaptorComponentScope<BsonRaptorComponent>.definitions(definitions: Iterable<RaptorBsonDefinition<*>>) {
	raptorComponentSelection {
		component.definitions += definitions
	}
}


@Raptor.Dsl3
fun RaptorComponentScope<BsonRaptorComponent>.providers(vararg providers: CodecProvider) {
	providers(providers.asIterable())
}


@Raptor.Dsl3
fun RaptorComponentScope<BsonRaptorComponent>.providers(providers: Iterable<CodecProvider>) {
	raptorComponentSelection {
		component.providers += providers
	}
}


@Raptor.Dsl3
fun RaptorComponentScope<BsonRaptorComponent>.registries(vararg registries: CodecRegistry) {
	registries(registries.asIterable())
}


@Raptor.Dsl3
fun RaptorComponentScope<BsonRaptorComponent>.registries(registries: Iterable<CodecRegistry>) {
	raptorComponentSelection {
		component.registries += registries
	}
}
