package io.fluidsonic.raptor


@RaptorDsl
public interface RaptorAssemblyInstallationScope : RaptorAssemblyScope, RaptorPluginScope<RaptorPluginWithConfiguration<*>> {

	@RaptorDsl
	public fun install(plugin: RaptorPluginWithConfiguration<*>)

	@RaptorDsl
	public fun optional(plugin: RaptorPluginWithConfiguration<*>, action: () -> Unit)

	@RaptorDsl
	public fun require(plugin: RaptorPluginWithConfiguration<*>, action: () -> Unit = {})
}
