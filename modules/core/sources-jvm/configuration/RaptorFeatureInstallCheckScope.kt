package io.fluidsonic.raptor


public interface RaptorFeatureInstallCheckScope {

	public fun Unit.ifInstalled(featureId: RaptorFeatureId, action: () -> Unit)
}


@RaptorDsl
public fun <Scope : RaptorFeatureInstallCheckScope> Scope.ifInstalled(featureId: RaptorFeatureId, action: Scope.() -> Unit) {
	with(Unit) {
		this.ifInstalled(featureId) {
			action()
		}
	}
}
