package io.fluidsonic.raptor


@RaptorDsl
public interface RaptorAssemblyInstallationScope : RaptorAssemblyScope {

	@RaptorDsl
	public fun install(plugin: RaptorPlugin)

	@RaptorDsl
	public fun optional(plugin: RaptorPlugin, action: () -> Unit)

	@RaptorDsl
	public fun require(plugin: RaptorPlugin, action: () -> Unit = {})
}
