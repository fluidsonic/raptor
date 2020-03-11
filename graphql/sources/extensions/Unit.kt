// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Unit@graph")

package io.fluidsonic.raptor


fun Unit.graphDefinition() = graphScalarDefinition {
	conversion<Unit> {
		// outputOnly() // FIXME

		parseInt { TODO() }
		parseJson<Any> { TODO() }
		serializeJson { 42 } // FIXME
	}
}
