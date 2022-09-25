package io.fluidsonic.raptor


@Deprecated(message = "Renamed to RaptorPlugin.", replaceWith = ReplaceWith("RaptorPlugin"))
public typealias RaptorFeature = RaptorPlugin


public interface RaptorPlugin : RaptorPluginWithConfiguration<Unit> {

	public override fun RaptorPluginCompletionScope.complete() {}
}


public interface RaptorPluginWithConfiguration<out Configuration : Any> {

	public fun RaptorPluginCompletionScope.complete(): Configuration
	public fun RaptorPluginInstallationScope.install()
}
