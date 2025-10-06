package io.fluidsonic.raptor.mongo2

import com.mongodb.client.model.*
import org.bson.*
import org.bson.conversions.*


public data class MongoReplaceOptions(
	val bypassDocumentValidation: Boolean = false,
	val collation: MongoCollation? = null,
	val comment: BsonValue? = null,
	val hint: Bson? = null,
	val hintString: String? = null,
	val let: Bson? = null,
	val sort: Bson? = null,
	val upsert: Boolean = false,
) {

	public companion object {

		public val empty: MongoReplaceOptions = MongoReplaceOptions()
	}
}


private val emptySource = ReplaceOptions()


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS") // Incorrect in Java Driver for `setLet()`.
internal fun MongoReplaceOptions.toSource(): ReplaceOptions =
	when {
		this === MongoReplaceOptions.empty -> emptySource // default case
		else -> ReplaceOptions()
			.bypassDocumentValidation(bypassDocumentValidation)
			.collation(collation?.toSource())
			.comment(comment)
			.hint(hint)
			.hintString(hintString)
			.let(let)
			.sort(sort)
			.upsert(upsert)
	}
