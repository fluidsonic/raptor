package io.fluidsonic.raptor


public interface RaptorAssemblyCompletionScope : RaptorAssemblyScope {

	@RaptorDsl
	public val lazyContext: RaptorContext

	@RaptorDsl
	public val propertyRegistry: RaptorPropertyRegistry

	@RaptorDsl
	public fun <Configuration : Any, Plugin : RaptorPluginWithConfiguration<Configuration>> require(plugin: Plugin): Configuration
}
