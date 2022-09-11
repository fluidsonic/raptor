package io.fluidsonic.raptor


public interface RaptorFeatureTestScope {

	@RaptorDsl
	public fun ifFeature(feature: RaptorFeature, action: () -> Unit)

	@RaptorDsl
	public fun requireFeature(feature: RaptorFeature, action: () -> Unit)
}
