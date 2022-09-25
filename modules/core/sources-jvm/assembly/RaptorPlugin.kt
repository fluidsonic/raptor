package io.fluidsonic.raptor


@Deprecated(message = "Renamed to RaptorPlugin.", replaceWith = ReplaceWith("RaptorPlugin"))
public typealias RaptorFeature = RaptorPlugin


public interface RaptorPlugin {

	public fun RaptorPluginCompletionScope.complete() {}
	public fun RaptorPluginInstallationScope.install()
}
