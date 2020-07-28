package io.fluidsonic.raptor

import org.kodein.di.*


object RaptorKodeinFeature : RaptorFeature {

	override val id = raptorKodeinFeatureId


	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(RootKodeinRaptorComponent.Key, RootKodeinRaptorComponent())
	}
}


const val raptorKodeinFeatureId: RaptorFeatureId = "raptor.kodein"


val Raptor.dkodein: DKodein
	get() = context.dkodein


val Raptor.kodein: Kodein
	get() = context.kodein


val RaptorScope.dkodein: DKodein
	get() = context.properties[DKodeinRaptorPropertyKey]
		?: error("You must install ${RaptorKodeinFeature::class.simpleName} for enabling Kodein functionality.")


val RaptorScope.kodein: Kodein
	get() = context.properties[KodeinRaptorPropertyKey]
		?: error("You must install ${RaptorKodeinFeature::class.simpleName} for enabling Kodein functionality.")


val RaptorTransaction.dkodein: DKodein
	get() = context.dkodein


val RaptorTransaction.kodein: Kodein
	get() = context.kodein


@RaptorDsl
fun RaptorTopLevelConfigurationScope.kodein(configuration: RaptorKodeinBuilder.() -> Unit) {
	componentRegistry.configure(RootKodeinRaptorComponent.Key) {
		configurations += configuration
	}
}
