package io.fluidsonic.raptor


@Deprecated(message = "Renamed to RaptorPluginInstallationScope.", replaceWith = ReplaceWith("RaptorPluginInstallationScope"))
public typealias RaptorFeatureScope = RaptorPluginInstallationScope


@RaptorDsl // FIXME Use DSL for what scopes?
public interface RaptorPluginInstallationScope : RaptorAssemblyInstallationScope
