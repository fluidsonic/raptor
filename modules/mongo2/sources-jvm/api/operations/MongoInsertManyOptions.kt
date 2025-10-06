package io.fluidsonic.raptor.mongo2

import com.mongodb.client.model.*
import org.bson.*


public data class MongoInsertManyOptions(
	val bypassDocumentValidation: Boolean = false,
	val comment: BsonValue? = null,
	val ordered: Boolean = true,
) {

	public companion object {

		public val empty: MongoInsertManyOptions = MongoInsertManyOptions()
	}
}


private val emptySource = InsertManyOptions()


internal fun MongoInsertManyOptions.toSource(): InsertManyOptions =
	when {
		this === MongoInsertManyOptions.empty -> emptySource // default case
		else -> InsertManyOptions()
			.bypassDocumentValidation(bypassDocumentValidation)
			.comment(comment)
			.ordered(ordered)
	}
