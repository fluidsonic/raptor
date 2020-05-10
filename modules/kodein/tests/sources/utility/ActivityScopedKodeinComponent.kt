package tests

import io.fluidsonic.raptor.*


class ActivityScopedKodeinComponent : RaptorComponent.Base<ActivityScopedKodeinComponent>(), RaptorKodeinGeneratingComponent {

	object Key : RaptorComponentKey<ActivityScopedKodeinComponent> {

		override fun toString() = "kodein (activity-scoped)"
	}
}


@RaptorDsl
val RaptorGlobalConfigurationScope.activities
	get() = componentRegistry.configure(ActivityScopedKodeinComponent.Key)
