package io.fluidsonic.raptor.mongo2

import com.mongodb.client.model.*
import org.bson.*


public data class MongoInsertOneOptions(
	val bypassDocumentValidation: Boolean = false,
	val comment: BsonValue? = null,
) {

	public companion object {

		public val empty: MongoInsertOneOptions = MongoInsertOneOptions()
	}
}


private val emptySource = InsertOneOptions()


internal fun MongoInsertOneOptions.toSource(): InsertOneOptions =
	when {
		this === MongoInsertOneOptions.empty -> emptySource // default case
		else -> InsertOneOptions()
			.bypassDocumentValidation(bypassDocumentValidation)
			.comment(comment)
	}
