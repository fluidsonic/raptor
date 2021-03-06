// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Int@graph")

package io.fluidsonic.raptor


public fun Int.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
	parseInt(::identity)
	serialize(::identity)
}
