package io.fluidsonic.raptor


inline class PasswordHash(val value: String) {

	override fun toString() =
		"PasswordHash(***)"


	companion object {

		fun bsonDefinition() = bsonDefinition(
			parse = ::PasswordHash,
			serialize = PasswordHash::value
		)
	}
}
