package io.fluidsonic.raptor


@Deprecated(message = "Renamed to RaptorPluginInstallationScope.", replaceWith = ReplaceWith("RaptorPluginInstallationScope"))
public typealias RaptorFeatureScope = RaptorPluginInstallationScope


@RaptorDsl // TODO Revisit what scope should use a DSL marker.
public interface RaptorPluginInstallationScope : RaptorAssemblyInstallationScope
