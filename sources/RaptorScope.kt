package io.fluidsonic.raptor

import org.kodein.di.*


interface RaptorScope : DKodeinAware {

	// FIXME move as this will bleed into every scope
	fun beginTransaction(): RaptorTransaction
}


//// FIXME move as this will bleed into every scope
//suspend inline fun <Result> RaptorScope.transaction(block: RaptorTransactionScope.() -> Result): Result {
//	val transaction = beginTransaction()
//	transaction.start()
//
//	var exception: Throwable? = null
//
//	try {
//		return with(transaction.context, block)
//	}
//	catch (e: Throwable) {
//		exception = e
//
//		throw e
//	}
//	finally {
//		if (exception != null)
//			try {
//				transaction.stop()
//			}
//			catch (e: Throwable) {
//				exception.addSuppressed(e)
//			}
//		else
//			transaction.stop()
//	}
//}
