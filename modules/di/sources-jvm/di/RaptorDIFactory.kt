package io.fluidsonic.raptor


// TODO Make public if it's actually useful and after API was revisited.
@InternalRaptorApi
public interface RaptorDIFactory {

	public fun createDI(context: RaptorContext, configuration: RaptorDIBuilder.() -> Unit = {}): RaptorDI

	public companion object
}
