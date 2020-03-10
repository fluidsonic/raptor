package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*


fun Cents.Companion.graphDefinition() = graphScalarDefinition {
	conversion<Cents> {
		parseString { it.toLongOrNull()?.let(::Cents) }

		parseJson<String> { it.toLongOrNull()?.let(::Cents) }
		serializeJson { it.value.toString() }
	}
}
