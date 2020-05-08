package io.fluidsonic.raptor

import org.bson.codecs.configuration.*
import org.kodein.di.*
import org.kodein.di.erased.*


internal class BsonScopeImpl(
	config: BsonConfig,
	scope: RaptorScope
) : BsonScope, RaptorScope by scope {

	override val dkodein = Kodein.direct(allowSilentOverride = true) {
		extend(context.dkodein)

		bind<BsonScope>() with instance(this@BsonScopeImpl)
	}


	// FIXME This doesn't maintain order between definitions, codecs & registries. How to handle overrides?
	override val codecRegistry = CodecRegistries.fromRegistries(
		*config.registries.toTypedArray(),
		CodecRegistries.fromProviders(*config.providers.toTypedArray()),
		CodecRegistries.fromCodecs(*config.codecs.toTypedArray()),
		CodecRegistries.fromCodecs(config.definitions.map { it.codec(this) })
	)!!
}
