package io.fluidsonic.raptor


// Inline classes are still broken in Kotlin 1.3.72
/* inline */ public data class AccessToken(val value: String) {

	override fun toString(): String = value


	public companion object {

		public fun graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
			parseString(::AccessToken)
			serialize(AccessToken::value)
		}
	}
}
