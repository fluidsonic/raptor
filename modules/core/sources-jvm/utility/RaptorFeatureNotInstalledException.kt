package io.fluidsonic.raptor


public class RaptorFeatureNotInstalledException(
	public val feature: RaptorFeature,
) : RuntimeException("Feature ${feature::class.qualifiedName ?: feature.toString()} is not installed.")
