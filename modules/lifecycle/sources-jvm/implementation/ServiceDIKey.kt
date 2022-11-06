package io.fluidsonic.raptor.lifecycle

import io.fluidsonic.raptor.di.*


internal class ServiceDIKey(
	val name: String,
) : RaptorDIKey<RaptorService> {

	override fun toString(): String =
		"RaptorService ($name)"
}
