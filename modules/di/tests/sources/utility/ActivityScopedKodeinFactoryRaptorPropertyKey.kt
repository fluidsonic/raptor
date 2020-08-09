package tests

import io.fluidsonic.raptor.*


object ActivityScopedKodeinFactoryRaptorPropertyKey : RaptorPropertyKey<RaptorKodeinFactory> {

	override fun toString() = "kodein factory (activity-scoped)"
}
