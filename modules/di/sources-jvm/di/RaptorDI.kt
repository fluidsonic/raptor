package io.fluidsonic.raptor.di

import io.fluidsonic.raptor.*
import kotlin.internal.*
import kotlin.properties.*
import kotlin.reflect.*


private val propertyKey = RaptorPropertyKey<RaptorDI>("DI")


@RaptorDsl
public interface RaptorDI {

	@RaptorDsl
	public fun <Value> get(key: RaptorDIKey<out Value>): Value


	@RaptorDsl
	public fun <Value : Any> getOrNull(key: RaptorDIKey<out Value?>): Value?


	@RaptorDsl
	public operator fun <Value> invoke(factory: RaptorDI.() -> Value): Lazy<Value>


	override fun toString(): String


	public companion object {

		@RaptorInternalApi
		public fun factory(modules: List<Module>): Factory =
			DefaultRaptorDI.Factory(modules = modules.toList())


		@RaptorInternalApi
		public fun module(name: String, providers: List<Provider<*>>): Module =
			DefaultRaptorDI.Module(name = name, providers = providers.toList())


		@RaptorInternalApi
		public fun <Value> provider(key: RaptorDIKey<Value>, provide: RaptorDI.() -> Value?): Provider<Value> =
			DefaultRaptorDI.Provider(provide = provide, key = key)
	}


	// TODO Make public if it's actually useful and after API was revisited.
	@RaptorInternalApi
	public interface Factory {

		public fun <Context : RaptorContext> createDI(
			context: Context,
			key: RaptorDIKey<in Context>,
			configuration: RaptorDIBuilder.() -> Unit = {},
		): RaptorDI


		public companion object {

			public val empty: Factory = DefaultRaptorDI.Factory(modules = emptyList())
		}
	}


	@RaptorInternalApi
	public interface Module {

		public val name: String
		public val providers: List<Provider<*>>
	}


	@RaptorInternalApi
	public interface Provider<Value> {

		public val key: RaptorDIKey<Value>

		public fun provide(di: RaptorDI): Value?
	}
}


@RaptorDsl
public val RaptorDI.context: RaptorContext
	get() = get()


@RaptorDsl
public inline fun <reified Value> RaptorDI.get(): Value =
	get(typeOf<Value>())


@RaptorDsl
public fun <Value> RaptorDI.get(type: KType): Value =
	get(RaptorDIKey<Value>(type))


@RaptorDsl
public inline fun <reified Value : Any> RaptorDI.getOrNull(): Value? =
	getOrNull(typeOf<Value>())


@RaptorDsl
public fun <Value : Any> RaptorDI.getOrNull(type: KType): Value? =
	getOrNull(RaptorDIKey<Value?>(type))


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
	createDI(context = context, key = RaptorDIKey<Context>(), configuration = configuration)


@Suppress("UNCHECKED_CAST")
public fun <Value> RaptorDI.Module.providerForKey(key: RaptorDIKey<out Value>): RaptorDI.Provider<Value>? =
	providers.lastOrNull { it.key == key } as RaptorDI.Provider<Value>?


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
