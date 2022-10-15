package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.transactions.*


private object RaptorExecutorContextExtensionKey : GExecutorContextExtensionKey<RaptorTransactionContext>


internal val GExecutorContext.raptorContext: RaptorTransactionContext?
	get() = extensions[RaptorExecutorContextExtensionKey]


internal var GExecutorContextExtensionSet.Builder.raptorContext: RaptorTransactionContext?
	get() = get(RaptorExecutorContextExtensionKey)
	set(value) {
		set(RaptorExecutorContextExtensionKey, value)
	}
