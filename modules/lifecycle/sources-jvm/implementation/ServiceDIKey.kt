package io.fluidsonic.raptor.lifecycle

import io.fluidsonic.raptor.di.*


internal class ServiceDIKey<Service : RaptorService>(
	val name: String,
) : RaptorDIKey<Service> {

	override val isOptional: Boolean
		get() = false


	override fun notOptional(): ServiceDIKey<Service> =
		this


	override fun toString(): String =
		"RaptorService ($name)"
}
