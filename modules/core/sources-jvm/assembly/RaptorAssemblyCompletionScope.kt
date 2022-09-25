package io.fluidsonic.raptor


public interface RaptorAssemblyCompletionScope : RaptorAssemblyScope {

	@RaptorDsl
	public val lazyContext: RaptorContext

	@RaptorDsl
	public val propertyRegistry: RaptorPropertyRegistry
}
