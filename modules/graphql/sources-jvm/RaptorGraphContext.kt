package io.fluidsonic.raptor

import io.fluidsonic.raptor.transactions.*


public interface RaptorGraphContext : RaptorTransactionContext, RaptorGraphScope {

	override fun toString(): String


	override fun asScope(): RaptorGraphScope =
		this


	override val context: RaptorGraphContext
		get() = this


	public companion object
}
