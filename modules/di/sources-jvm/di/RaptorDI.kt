package io.fluidsonic.raptor

import kotlin.internal.*
import kotlin.properties.*
import kotlin.reflect.*
import kotlin.reflect.full.*


public interface RaptorDI {

	@RaptorDsl
	public fun get(type: KType): Any?


	@RaptorDsl
	public operator fun <Value> invoke(factory: RaptorDI.() -> Value): Lazy<Value>


	override fun toString(): String


	public companion object {

		@InternalRaptorApi
		public fun factory(modules: List<Module>): Factory =
			DefaultRaptorDI.Factory(modules = modules.toList())

		@InternalRaptorApi
		public fun module(name: String, providers: List<Provider>): Module =
			DefaultRaptorDI.Module(name = name, providers = providers.toList())


		@InternalRaptorApi
		public fun provider(type: KType, provide: RaptorDI.() -> Any?): Provider =
			DefaultRaptorDI.Provider(provide = provide, type = type)
	}


	// TODO Make public if it's actually useful and after API was revisited.
	@InternalRaptorApi
	public interface Factory {

		public fun createDI(context: RaptorContext, configuration: RaptorDIBuilder.() -> Unit = {}): RaptorDI
	}


	@InternalRaptorApi
	public interface Module {

		public val name: String
		public val providers: List<Provider>
	}


	@InternalRaptorApi
	public interface Provider {

		public val type: KType

		public fun provide(di: RaptorDI): Any?
	}
}


@OptIn(ExperimentalStdlibApi::class)
@RaptorDsl
public inline fun <reified Value> RaptorDI.get(): Value =
	get(typeOf<Value>()) as Value


@OptIn(ExperimentalStdlibApi::class)
@RaptorDsl
public inline operator fun <reified Value> RaptorDI.invoke(): PropertyDelegateProvider<Any?, Lazy<Value>> =
	object : PropertyDelegateProvider<Any?, Lazy<Value>> {

		private val type = typeOf<Value>()


		override fun provideDelegate(thisRef: Any?, property: KProperty<*>): Lazy<Value> =
			lazy { get(type) as Value }
	}


public fun RaptorDI.Module.providerForType(type: KType): RaptorDI.Provider? =
	providers.lastOrNull { it.type.isSubtypeOf(type) }


@RaptorDsl
public val RaptorScope.di: RaptorDI
	get() = context.properties[DIRaptorPropertyKey]
		?: error("You must install ${RaptorDIFeature::class.simpleName} for enabling dependency injection functionality.")


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
