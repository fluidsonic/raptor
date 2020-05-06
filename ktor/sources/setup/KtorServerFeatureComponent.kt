package io.fluidsonic.raptor


@Raptor.Dsl3
class KtorServerFeatureComponent internal constructor(
	globalComponent: RaptorFeatureSetup,
	serverComponent: KtorServerRaptorComponent
) : RaptorComponent {

	@Raptor.Dsl3
	val global = RaptorComponentConfig.of(globalComponent)


	@Raptor.Dsl3
	val server = RaptorComponentConfig.of(serverComponent)
}
