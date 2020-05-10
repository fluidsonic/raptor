package io.fluidsonic.raptor

import org.kodein.di.*


object RaptorKodeinFeature : RaptorFeature {

	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(RootKodeinRaptorComponent.Key, RootKodeinRaptorComponent())
	}
}


val Raptor.kodein: Kodein
	get() = context.kodein


val RaptorContext.kodein: Kodein
	get() = properties[KodeinRaptorPropertyKey]
		?: error("You must install ${RaptorKodeinFeature::class.simpleName} for enabling Kodein functionality.")


val RaptorTransaction.kodein: Kodein
	get() = context.kodein


@RaptorDsl
fun RaptorTopLevelConfigurationScope.kodein(configuration: RaptorKodeinBuilder.() -> Unit) {
	componentRegistry.configure(RootKodeinRaptorComponent.Key) {
		configurations += configuration
	}
}
