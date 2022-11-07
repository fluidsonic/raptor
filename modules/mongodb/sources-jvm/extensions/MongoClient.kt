package io.fluidsonic.raptor.mongo

import com.mongodb.*
import io.fluidsonic.mongo.*


public suspend inline fun MongoClient.transaction(
	options: TransactionOptions = TransactionOptions.builder().build(),
	block: (session: ClientSession) -> Unit,
) {
	startSession().use { session ->
		session.startTransaction(options)

		try {
			block(session)
		}
		catch (e: Throwable) {
			session.abortTransaction()
			throw e
		}

		session.commitTransaction()
	}
}
