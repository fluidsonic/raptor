package io.fluidsonic.raptor


// Inline classes are still broken in Kotlin 1.3.72
/* inline */ data class PasswordHash(val value: String) {

	override fun toString() =
		"PasswordHash(***)"


	companion object {

		fun bsonDefinition() = bsonDefinition(
			parse = ::PasswordHash,
			serialize = PasswordHash::value
		)
	}
}
