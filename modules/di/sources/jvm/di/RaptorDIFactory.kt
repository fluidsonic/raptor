package io.fluidsonic.raptor


// TODO Make public if it's actually useful and after API was revisited.
internal interface RaptorDIFactory {

	fun createDI(context: RaptorContext, configuration: RaptorDIBuilder.() -> Unit = {}): RaptorDI

	companion object
}
