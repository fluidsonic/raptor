package tests

import io.fluidsonic.raptor.*
import org.kodein.di.*
import org.kodein.di.erased.*


object ActivityScopedKodeinFeature : RaptorFeature {

	override fun RaptorFeatureConfigurationEndScope.onConfigurationEnded() {
		propertyRegistry.register(ActivityScopedKodeinFactoryRaptorPropertyKey, kodeinFactory(
			name = "activity-scoped",
			component = componentRegistry.one(ActivityScopedKodeinComponent.Key)
		))
	}


	override fun RaptorFeatureConfigurationStartScope.onConfigurationStarted() {
		componentRegistry.register(ActivityScopedKodeinComponent.Key, ActivityScopedKodeinComponent())
	}


	override fun toString() = "activity-scoped kodein feature"
}


fun Raptor.createKodein(activity: Activity): Kodein =
	context.createKodein(activity)


fun RaptorContext.createKodein(activity: Activity): Kodein = // FIXME
	properties[ActivityScopedKodeinFactoryRaptorPropertyKey]?.createKodein(context = this) {
		bind() from instance(activity)
	}
		?: error("You must install ActivityScopedKodeinFeature for enabling activity-scoped Kodein functionality.")


fun RaptorTransaction.createKodein(activity: Activity): Kodein =
	context.createKodein(activity)
