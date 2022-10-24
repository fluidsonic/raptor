package io.fluidsonic.raptor.di

import io.fluidsonic.raptor.*


internal class RaptorDIFactoryComponent :
	RaptorComponent.Base<RaptorDIFactoryComponent>(RaptorDIPlugin),
	RaptorDIComponent<RaptorDIFactoryComponent> {

	private val builder = DefaultRaptorDIBuilder()


	override fun <Value : Any> provide(key: RaptorDIKey<in Value>, provide: RaptorDI.() -> Value) {
		builder.provide(key = key, provide = provide)
	}


	override fun <Value : Any> provideOptional(key: RaptorDIKey<in Value>, provide: RaptorDI.() -> Value?) {
		builder.provide(key = key, provide = provide)
	}


	fun toFactory(name: String): RaptorDI.Factory =
		DefaultRaptorDI.Factory(modules = listOf(builder.createModule(name = name)))


	override fun toString(): String =
		"DI factory configuration"
}
