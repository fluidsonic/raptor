package io.fluidsonic.raptor.mongo2

import com.mongodb.client.model.*
import org.bson.*
import org.bson.conversions.*


public data class MongoDeleteOptions(
	val hint: Bson? = null,
	val hintString: String? = null,
	val collation: Collation? = null,
	val comment: BsonValue? = null,
	val let: Bson? = null,
) {

	public companion object {

		public val empty: MongoDeleteOptions = MongoDeleteOptions()
	}
}


private val emptySource = DeleteOptions()


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS") // Incorrect in Java Driver for `setLet()`.
internal fun MongoDeleteOptions.toSource(): DeleteOptions =
	when {
		this === MongoDeleteOptions.empty -> emptySource // default case
		else -> DeleteOptions()
			.hint(hint)
			.hintString(hintString)
			.collation(collation)
			.comment(comment)
			.let(let)
	}
