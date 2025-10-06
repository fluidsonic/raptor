package io.fluidsonic.raptor.mongo2

import com.mongodb.client.model.*


public data class MongoCollation(
	val alternate: CollationAlternate? = null,
	val backwards: Boolean? = null,
	val caseFirst: CollationCaseFirst? = null,
	val caseLevel: Boolean? = null,
	val locale: String? = null,
	val maxVariable: CollationMaxVariable? = null,
	val normalization: Boolean? = null,
	val numericOrdering: Boolean? = null,
	val strength: CollationStrength? = null,
)


internal fun MongoCollation.toSource(): Collation =
	Collation.builder()
		.backwards(backwards)
		.caseLevel(caseLevel)
		.collationAlternate(alternate)
		.collationCaseFirst(caseFirst)
		.collationMaxVariable(maxVariable)
		.collationStrength(strength)
		.locale(locale)
		.normalization(normalization)
		.numericOrdering(numericOrdering)
		.build()
