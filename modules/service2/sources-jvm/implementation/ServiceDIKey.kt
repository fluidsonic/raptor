package io.fluidsonic.raptor.service2

import io.fluidsonic.raptor.di.*


internal data class ServiceDIKey2<Service : RaptorService2>(
	val name: String,
) : RaptorDIKey<Service> {

	override val isOptional: Boolean
		get() = false


	override fun notOptional(): ServiceDIKey2<Service> =
		this


	override fun toString(): String =
		"RaptorService2 ($name)"
}
