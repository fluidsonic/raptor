package io.fluidsonic.raptor


fun Unit.graphDefinition() = graphScalarDefinition {
	conversion<Unit> {
		// outputOnly() // FIXME

		parseInt { TODO() }
		parseJson<Any> { TODO() }
		serializeJson { 42 } // FIXME
	}
}
