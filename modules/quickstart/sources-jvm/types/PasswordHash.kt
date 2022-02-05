package io.fluidsonic.raptor


@JvmInline
public value class PasswordHash(public val value: String) {

	override fun toString(): String =
		"PasswordHash(***)"


	public companion object {

		public fun bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<PasswordHash> {
			decode(::PasswordHash)
			encode(PasswordHash::value)
		}
	}
}
