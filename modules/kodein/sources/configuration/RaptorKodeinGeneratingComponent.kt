package io.fluidsonic.raptor

import org.kodein.di.*


interface RaptorKodeinGeneratingComponent : RaptorComponent {

	companion object
}


@RaptorDsl
fun RaptorComponentSet<RaptorKodeinGeneratingComponent>.kodein(configuration: RaptorKodeinBuilder.() -> Unit) = configure {
	componentRegistry.oneOrRegister(KodeinFactoryRaptorComponent.Key) { KodeinFactoryRaptorComponent() }.configure {
		configurations += configuration
	}
}


// FIXME throw if feature not installed?
@RaptorDsl
@Suppress("unused")
fun RaptorFeatureConfigurationEndScope.kodeinFactory(name: String, component: RaptorKodeinGeneratingComponent): RaptorKodeinFactory =
	component.componentRegistry.oneOrNull(KodeinFactoryRaptorComponent.Key)?.toFactory(name = name)
		?: DefaultRaptorKodeinFactory(module = Kodein.Module(name) {})
