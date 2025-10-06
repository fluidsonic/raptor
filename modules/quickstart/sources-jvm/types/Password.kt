package io.fluidsonic.raptor

import io.fluidsonic.raptor.graph.*


@JvmInline
public value class Password(public val value: String) {

	public inline fun ifEmpty(block: () -> Unit): Password = apply {
		if (isEmpty()) block()
	}


	public fun isEmpty(): Boolean =
		value.isEmpty()


	public fun isNotEmpty(): Boolean =
		!isEmpty()


	override fun toString(): String =
		"••••••"


	public companion object {

		public fun graphDefinition(): RaptorGraphDefinition = graphScalarDefinition<Password> {
			parseString(::Password)
			serialize(Password::value)
		}
	}
}
