package io.fluidsonic.raptor


// Inline classes are still broken in Kotlin 1.3.72
/* inline */ public data class PasswordHash(val value: String) {

	override fun toString(): String =
		"PasswordHash(***)"


	public companion object {

		public fun bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<PasswordHash> {
			decode(::PasswordHash)
			encode(PasswordHash::value)
		}
	}
}
