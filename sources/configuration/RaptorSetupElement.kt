package io.fluidsonic.raptor


interface RaptorSetupElement : RaptorSetupScope {

	val raptorSetupContext: RaptorSetupContext


	interface Taggable : RaptorSetupElement
}
