package io.fluidsonic.raptor

import org.kodein.di.*


@RaptorDsl
interface RaptorKodeinGeneratingComponent : RaptorComponent {

	companion object
}


@RaptorDsl
fun RaptorComponentSet<RaptorKodeinGeneratingComponent>.kodein(configuration: RaptorKodeinBuilder.() -> Unit) = configure {
	componentRegistry.oneOrRegister(DefaultGeneratingKodeinRaptorComponent.Key) { DefaultGeneratingKodeinRaptorComponent() }.configure {
		configurations += configuration
	}
}


@RaptorDsl
@Suppress("unused")
fun RaptorFeatureFinalizationScope.kodeinFactory(name: String, component: RaptorKodeinGeneratingComponent): RaptorKodeinFactory =
	component.componentRegistry.oneOrNull(DefaultGeneratingKodeinRaptorComponent.Key)?.finalize(name = name)
		?: DefaultRaptorKodeinFactory(module = Kodein.Module(name) {})
