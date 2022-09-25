package io.fluidsonic.raptor.di

import io.fluidsonic.raptor.*
import kotlin.internal.*
import kotlin.properties.*
import kotlin.reflect.*


private val propertyKey = RaptorPropertyKey<RaptorDI>("DI")


@RaptorDsl
public interface RaptorDI {

	@RaptorDsl
	public fun get(type: KType): Any?


	@RaptorDsl
	public operator fun <Value> invoke(factory: RaptorDI.() -> Value): Lazy<Value>


	override fun toString(): String


	public companion object {

		@RaptorInternalApi
		public fun factory(modules: List<Module>): Factory =
			DefaultRaptorDI.Factory(modules = modules.toList())


		@RaptorInternalApi
		public fun module(name: String, providers: List<Provider>): Module =
			DefaultRaptorDI.Module(name = name, providers = providers.toList())


		@RaptorInternalApi
		public fun provider(type: KType, provide: RaptorDI.() -> Any?): Provider =
			DefaultRaptorDI.Provider(provide = provide, type = type)
	}


	// TODO Make public if it's actually useful and after API was revisited.
	@RaptorInternalApi
	public interface Factory {

		public fun createDI(context: RaptorContext, type: KType, configuration: RaptorDIBuilder.() -> Unit = {}): RaptorDI


		public companion object {

			public val empty: Factory = DefaultRaptorDI.Factory(modules = emptyList())
		}
	}


	@RaptorInternalApi
	public interface Module {

		public val name: String
		public val providers: List<Provider>
	}


	@RaptorInternalApi
	public interface Provider {

		public val type: KType

		public fun provide(di: RaptorDI): Any?
	}
}


@RaptorDsl
public val RaptorDI.context: RaptorContext
	get() = get()


@RaptorDsl
public inline fun <reified Value> RaptorDI.get(): Value =
	get(typeOf<Value>()) as Value


@RaptorDsl
public inline operator fun <reified Value> RaptorDI.invoke(): PropertyDelegateProvider<Any?, Lazy<Value>> =
	object : PropertyDelegateProvider<Any?, Lazy<Value>> {

		private val type = typeOf<Value>()


		override fun provideDelegate(thisRef: Any?, property: KProperty<*>): Lazy<Value> =
			lazy { get(type) as Value }
	}


@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
internal inline fun <reified Context : RaptorContext> RaptorDI.Factory.createDI(
	context: @NoInfer Context,
	noinline configuration: RaptorDIBuilder.() -> Unit = {},
): RaptorDI =
	createDI(context = context, type = typeOf<Context>(), configuration = configuration)


public fun RaptorDI.Module.providerForType(type: KType): RaptorDI.Provider? =
	providers.lastOrNull { it.type == type }


internal fun RaptorPropertyRegistry.register(di: RaptorDI) {
	register(propertyKey, di)
}


@RaptorDsl
public val RaptorScope.di: RaptorDI
	get() = context.properties[propertyKey] ?: throw RaptorPluginNotInstalledException(RaptorDIPlugin)


@LowPriorityInOverloadResolution
@RaptorDsl
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
public inline fun <reified Value> RaptorScope.di(): PropertyDelegateProvider<Any?, Lazy<Value>> =
	di.invoke()


@LowPriorityInOverloadResolution
@RaptorDsl
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
public inline fun <reified Value> RaptorScope.di(
	noinline factory: RaptorDI.() -> Value,
): Lazy<Value> =
	di.invoke(factory)
