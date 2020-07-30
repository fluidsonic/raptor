package io.fluidsonic.raptor.graphql.internal

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.*


private object RaptorExecutorContextExtensionKey : GExecutorContextExtensionKey<RaptorGraphContext>


internal val GExecutorContext.raptorContext: RaptorGraphContext?
	get() = extensions[RaptorExecutorContextExtensionKey]


internal var GExecutorContextExtensionSet.Builder.raptorContext: RaptorGraphContext?
	get() = get(RaptorExecutorContextExtensionKey)
	set(value) {
		set(RaptorExecutorContextExtensionKey, value)
	}
