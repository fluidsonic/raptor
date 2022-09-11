package io.fluidsonic.raptor.transactions

import io.fluidsonic.raptor.*


public interface RaptorTransactionContext : RaptorContext, RaptorTransactionScope {

	override fun toString(): String


	override fun asScope(): RaptorTransactionScope =
		this


	override val context: RaptorTransactionContext
		get() = this


	override val parent: RaptorContext


	public companion object;


	public interface Lazy : RaptorTransactionContext, RaptorContext.Lazy
}
