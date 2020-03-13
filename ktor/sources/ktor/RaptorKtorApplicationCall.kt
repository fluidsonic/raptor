package io.fluidsonic.raptor

import io.ktor.application.*
import org.kodein.di.*
import org.kodein.di.erased.*


@Raptor.Dsl3
typealias RaptorKtorApplicationCall = ApplicationCall


val RaptorKtorApplicationCall.raptorContext: KtorServerContext
	get() = raptorKtorServerTransaction.context


val RaptorKtorApplicationCall.dkodein
	get() = raptorContext.dkodein


val RaptorKtorApplicationCall.kodein
	get() = dkodein.kodein


inline fun <reified A, reified T : Any> RaptorKtorApplicationCall.factory(tag: Any? = null) =
	dkodein.factory<A, T>(tag = tag)


inline fun <reified A, reified T : Any> RaptorKtorApplicationCall.factoryOrNull(tag: Any? = null) =
	dkodein.factoryOrNull<A, T>(tag = tag)


inline fun <reified T : Any> RaptorKtorApplicationCall.provider(tag: Any? = null) =
	dkodein.provider<T>(tag = tag)


inline fun <reified A, reified T : Any> RaptorKtorApplicationCall.provider(tag: Any? = null, arg: A) =
	dkodein.provider<A, T>(tag = tag, arg = arg)


inline fun <A, reified T : Any> RaptorKtorApplicationCall.provider(tag: Any? = null, arg: Typed<A>) =
	dkodein.provider<A, T>(tag = tag, arg = arg)


inline fun <reified A, reified T : Any> RaptorKtorApplicationCall.provider(tag: Any? = null, noinline fArg: () -> A) =
	dkodein.provider<A, T>(tag = tag, fArg = fArg)


inline fun <reified T : Any> RaptorKtorApplicationCall.providerOrNull(tag: Any? = null) =
	dkodein.providerOrNull<T>(tag = tag)


inline fun <reified A, reified T : Any> RaptorKtorApplicationCall.providerOrNull(tag: Any? = null, arg: A) =
	dkodein.providerOrNull<A, T>(tag = tag, arg = arg)


inline fun <A, reified T : Any> RaptorKtorApplicationCall.providerOrNull(tag: Any? = null, arg: Typed<A>) =
	dkodein.providerOrNull<A, T>(tag = tag, arg = arg)


inline fun <reified A, reified T : Any> RaptorKtorApplicationCall.providerOrNull(tag: Any? = null, noinline fArg: () -> A) =
	dkodein.providerOrNull<A, T>(tag = tag, fArg = fArg)


inline fun <reified T : Any> RaptorKtorApplicationCall.instance(tag: Any? = null) =
	dkodein.instance<T>(tag = tag)


inline fun <reified A, reified T : Any> RaptorKtorApplicationCall.instance(tag: Any? = null, arg: A) =
	dkodein.instance<A, T>(tag = tag, arg = arg)


inline fun <A, reified T : Any> RaptorKtorApplicationCall.instance(tag: Any? = null, arg: Typed<A>) =
	dkodein.instance<A, T>(tag = tag, arg = arg)


inline fun <reified T : Any> RaptorKtorApplicationCall.instanceOrNull(tag: Any? = null) =
	dkodein.instanceOrNull<T>(tag = tag)


inline fun <reified A, reified T : Any> RaptorKtorApplicationCall.instanceOrNull(tag: Any? = null, arg: A) =
	dkodein.instanceOrNull<A, T>(tag = tag, arg = arg)


inline fun <A, reified T : Any> RaptorKtorApplicationCall.instanceOrNull(tag: Any? = null, arg: Typed<A>) =
	dkodein.instanceOrNull<A, T>(tag = tag, arg = arg)


fun <T> RaptorKtorApplicationCall.newInstance(creator: DKodein.() -> T) =
	dkodein.newInstance(creator = creator)
