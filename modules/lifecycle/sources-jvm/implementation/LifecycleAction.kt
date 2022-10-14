package io.fluidsonic.raptor.lifecycle

import io.fluidsonic.raptor.*


internal data class LifecycleAction<in Scope : RaptorLifecycleScope>(
	val block: suspend Scope.() -> Unit,
	val priority: Int,
)
