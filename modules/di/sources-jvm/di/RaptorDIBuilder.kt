package io.fluidsonic.raptor.di

import io.fluidsonic.raptor.*


@RaptorDsl
public interface RaptorDIBuilder {

	@RaptorDsl
	public fun <Value : Any> provide(key: RaptorDIKey<in Value>, provide: RaptorDI.() -> Value?)
}


@RaptorDsl
public inline fun <reified Value : Any> RaptorDIBuilder.provide(noinline provide: RaptorDI.() -> Value) {
	provide(RaptorDIKey<Value>(), provide = provide)
}


@RaptorDsl
public inline fun <reified Value : Any> RaptorDIBuilder.provide(value: Value) {
	provide { value }
}


@RaptorDsl
public inline fun <reified Value : Any> RaptorDIBuilder.provideOptional(noinline provide: RaptorDI.() -> Value?) {
	provide(RaptorDIKey<Value>(), provide = provide)
}


@RaptorDsl
public inline fun <reified Value : Any> RaptorDIBuilder.provideOptional(value: Value?) {
	provideOptional { value }
}
