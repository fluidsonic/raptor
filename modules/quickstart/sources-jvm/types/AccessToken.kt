package io.fluidsonic.raptor

import io.fluidsonic.raptor.graph.*


@JvmInline
public value class AccessToken(public val value: String) {

	override fun toString(): String =
		"••••••"


	public companion object {

		public fun graphDefinition(): RaptorGraphDefinition = graphScalarDefinition<AccessToken> {
			parseString(::AccessToken)
			serialize(AccessToken::value)
		}
	}
}
