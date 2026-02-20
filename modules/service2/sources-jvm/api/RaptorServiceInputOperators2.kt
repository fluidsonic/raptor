package io.fluidsonic.raptor.service2

import io.fluidsonic.raptor.*
import io.fluidsonic.time.*
import kotlin.experimental.*
import kotlin.reflect.*
import kotlin.time.*
import kotlinx.coroutines.flow.*


@RaptorDsl
public fun <Service : RaptorService2, Value> RaptorServiceInput2<Service, Value>.batchBy(
	timeout: Duration,
	keySelector: context(Service) (Value) -> Any?,
): RaptorServiceInput2<Service, List<Value>> =
	BatchByInputSource(this, keySelector, timeout)


@RaptorDsl
public fun <Service : RaptorService2, Value> RaptorServiceInput2<Service, Value>.delay(
	duration: Duration,
): RaptorServiceInput2<Service, Value> =
	DelayInputSource(duration, this)


@RaptorDsl
public fun <Service : RaptorService2, Value> RaptorServiceInput2<Service, Value>.delayUntil(
	block: context(Service) (Value) -> Timestamp,
): RaptorServiceInput2<Service, Value> =
	DelayUntilInputSource(this, block)


/**
 * Suspends handling of items from the receiver until [sourceToWaitFor] emits its first value,
 * then releases all buffered items and passes through future items immediately.
 *
 * Note: this waits for **first emission** only, not for completion of [sourceToWaitFor].
 */
@RaptorDsl
public fun <Service : RaptorService2, Value> RaptorServiceInput2<Service, Value>.waitFor(
	sourceToWaitFor: RaptorServiceInput2<*, *>,
): RaptorServiceInput2<Service, Value> =
	WaitingInputSource(this, sourceToWaitFor)


@RaptorDsl
context (component: RaptorServiceComponent2<Service>)
public fun <Service : RaptorService2, Value> RaptorServiceInput2<Service, Value>.handle(
	handler: suspend Service.() -> Unit,
) {
	component.addInputSource(this) { handler() }
}


@kotlin.internal.LowPriorityInOverloadResolution
@RaptorDsl
@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
context (component: RaptorServiceComponent2<Service>)
public fun <Service : RaptorService2, Value> RaptorServiceInput2<Service, Value>.handle(
	handler: suspend Service.(Value) -> Unit,
) {
	component.addInputSource(this, handler)
}


@RaptorDsl
context (component: RaptorServiceComponent2<Service>)
public fun <Service : RaptorService2, A, B> RaptorServiceInput2<Service, Pair<A, B>>.handle(
	handler: suspend Service.(A, B) -> Unit,
) {
	handle { (a, b) -> handler(a, b) }
}


@RaptorDsl
context (component: RaptorServiceComponent2<Service>)
public fun <Service : RaptorService2, A, B, C> RaptorServiceInput2<Service, Triple<A, B, C>>.handle(
	handler: suspend Service.(A, B, C) -> Unit,
) {
	handle { (a, b, c) -> handler(a, b, c) }
}


@RaptorDsl
context (_: RaptorServiceComponent2<Service>)
public fun <Value, Service : RaptorService2> RaptorServiceInput2<Service, Value>.filter(
	predicate: suspend Service.(Value) -> Boolean,
): RaptorServiceInput2<Service, Value> =
	FilteredInputSource(predicate, this)


@RaptorDsl
context (_: RaptorServiceComponent2<Service>)
public fun <Value, Service : RaptorService2> RaptorServiceInput2<Service, Value>.filterNot(
	predicate: suspend context(Service) (Value) -> Boolean,
): RaptorServiceInput2<Service, Value> =
	FilteredInputSource({ !predicate(it) }, this)


@RaptorDsl
context (_: RaptorServiceComponent2<Service>)
public fun <Value, TransformedValue, Service : RaptorService2> RaptorServiceInput2<Service, Value>.map(
	transform: suspend Service.(Value) -> TransformedValue,
): RaptorServiceInput2<Service, TransformedValue> =
	TransformedInputSource(this, transform)


@RaptorDsl
context (_: RaptorServiceComponent2<Service>)
public fun <Value, TransformedValue, Service : RaptorService2> RaptorServiceInput2<Service, Value>.map(
	transform: KFunction1<Value, TransformedValue>,
): RaptorServiceInput2<Service, TransformedValue> =
	map { transform(it) }


@RaptorDsl
context (_: RaptorServiceComponent2<Service>)
public fun <Value, TransformedValue : Any, Service : RaptorService2> RaptorServiceInput2<Service, Value>.mapNotNull(
	transform: suspend Service.(Value) -> TransformedValue?,
): RaptorServiceInput2<Service, TransformedValue> =
	MapNotNullInputSource(this, transform)


@JvmName("flatMapIterable")
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@RaptorDsl
context (_: RaptorServiceComponent2<Service>)
public fun <Value, TransformedValue, Service : RaptorService2> RaptorServiceInput2<Service, Value>.flatMap(
	transform: suspend Service.(Value) -> Iterable<TransformedValue>,
): RaptorServiceInput2<Service, TransformedValue> =
	FlatMapIterableInputSource(this, transform)


@JvmName("flatMapFlow")
@OptIn(ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
@RaptorDsl
context (_: RaptorServiceComponent2<Service>)
public fun <Value, TransformedValue, Service : RaptorService2> RaptorServiceInput2<Service, Value>.flatMap(
	transform: suspend Service.(Value) -> Flow<TransformedValue>,
): RaptorServiceInput2<Service, TransformedValue> =
	FlatMapFlowInputSource(this, transform)
