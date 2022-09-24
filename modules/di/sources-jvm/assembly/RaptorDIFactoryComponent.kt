package io.fluidsonic.raptor.di

import io.fluidsonic.raptor.*
import kotlin.reflect.*


internal class RaptorDIFactoryComponent :
	RaptorComponent.Base<RaptorDIFactoryComponent>(),
	RaptorDIComponent<RaptorDIFactoryComponent> {

	private val builder = DefaultRaptorDIBuilder()


	override fun provide(type: KType, provide: RaptorDI.() -> Any?) {
		builder.provide(type = type, provide = provide)
	}


	fun toFactory(name: String): RaptorDI.Factory =
		DefaultRaptorDI.Factory(modules = listOf(builder.createModule(name = name)))


	override fun toString(): String =
		"DI factory configuration"
}
