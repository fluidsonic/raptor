package io.fluidsonic.raptor


@Deprecated(message = "Renamed to RaptorPluginInstallationScope.", replaceWith = ReplaceWith("RaptorPluginInstallationScope"))
public typealias RaptorFeatureScope = RaptorPluginInstallationScope


@RaptorDsl
public interface RaptorPluginInstallationScope : RaptorAssemblyInstallationScope
