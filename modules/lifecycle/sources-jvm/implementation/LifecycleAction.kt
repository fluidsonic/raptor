package io.fluidsonic.raptor.lifecycle


internal data class LifecycleAction<in Scope : RaptorLifecycleScope>(
	val block: suspend Scope.() -> Unit,
	val label: String,
	val priority: Int,
)
