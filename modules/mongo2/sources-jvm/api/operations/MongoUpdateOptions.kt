package io.fluidsonic.raptor.mongo2

import com.mongodb.client.model.*
import org.bson.*
import org.bson.conversions.*


public data class MongoUpdateOptions(
	val upsert: Boolean = false,
	val bypassDocumentValidation: Boolean = false,
	val collation: MongoCollation? = null,
	val arrayFilters: List<Bson?>? = null,
	val hint: Bson? = null,
	val hintString: String? = null,
	val comment: BsonValue? = null,
	val let: Bson? = null,
	val sort: Bson? = null,
) {

	public companion object {

		public val empty: MongoUpdateOptions = MongoUpdateOptions()
	}
}


private val emptySource = UpdateOptions()


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS") // Incorrect in Java Driver for `setLet()`.
internal fun MongoUpdateOptions.toSource(): UpdateOptions =
	when {
		this === MongoUpdateOptions.empty -> emptySource // default case
		else -> UpdateOptions()
			.arrayFilters(arrayFilters)
			.bypassDocumentValidation(bypassDocumentValidation)
			.collation(collation?.toSource())
			.comment(comment)
			.hint(hint)
			.hintString(hintString)
			.let(let)
			.sort(sort)
			.upsert(upsert)
	}
