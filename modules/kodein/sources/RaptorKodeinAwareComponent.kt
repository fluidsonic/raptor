package io.fluidsonic.raptor


// FIXME
@RaptorDsl
interface RaptorKodeinAwareComponent : RaptorComponent


@RaptorDsl
fun RaptorComponentSet<RaptorKodeinAwareComponent>.kodein(configuration: RaptorKodeinBuilder.() -> Unit) = configure {
	extensions.getOrSet(DefaultRaptorComponentKodeinExtension.Key) { DefaultRaptorComponentKodeinExtension() }
		.configurations
		.add(configuration)
}
