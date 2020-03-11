package io.fluidsonic.raptor


@Raptor.Dsl3
interface RaptorComponent {

	val raptorSetupContext: RaptorSetupContext // FIXME can we remove this?


	interface Taggable : RaptorComponent
}
