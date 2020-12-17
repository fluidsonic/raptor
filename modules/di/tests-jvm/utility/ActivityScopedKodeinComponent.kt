package tests

import io.fluidsonic.raptor.*


class ActivityScopedKodeinComponent : RaptorComponent.Default<ActivityScopedKodeinComponent>(), RaptorKodeinGeneratingComponent {

	object Key : RaptorComponentKey<ActivityScopedKodeinComponent> {

		override fun toString() = "kodein (activity-scoped)"
	}
}


@RaptorDsl
val RaptorTopLevelConfigurationScope.activities
	get() = componentRegistry.configure(ActivityScopedKodeinComponent.Key)
