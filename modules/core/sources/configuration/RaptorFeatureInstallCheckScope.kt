package io.fluidsonic.raptor


interface RaptorFeatureInstallCheckScope {

	fun Unit.ifInstalled(featureId: RaptorFeatureId, action: () -> Unit)
}


@RaptorDsl
fun <Scope : RaptorFeatureInstallCheckScope> Scope.ifInstalled(featureId: RaptorFeatureId, action: Scope.() -> Unit) {
	with(Unit) {
		this.ifInstalled(featureId) {
			action()
		}
	}
}
