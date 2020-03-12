package io.fluidsonic.raptor

import org.kodein.di.*


@Raptor.Dsl3
interface RaptorScope : DKodeinAware {

	val context: RaptorContext

	override val dkodein: DKodein
		get() = context.dkodein
}


@Raptor.Dsl3
inline fun <Result> RaptorScope.withNewTransaction(block: RaptorTransactionScope.() -> Result): Result =
	with(context.createTransaction().context.createScope()) {
		block()
	}
