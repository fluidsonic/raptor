package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*


internal class RaptorKtorRouteContext(
	override val parent: RaptorContext,
	override val properties: RaptorPropertySet,
) : RaptorContext {

	override fun toString(): String =
		parent.toString() // TODO Support hierarchical toString(). `this.properties` are missing.
}
