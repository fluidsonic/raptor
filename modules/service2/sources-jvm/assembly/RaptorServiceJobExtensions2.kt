package io.fluidsonic.raptor.service2

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.jobs2.*
import kotlinx.coroutines.*


// FIXME keyed job -> execute in order?

// FIXME all naming
// Note: Jobs are not aggregates, so RaptorServiceInputOnJob is a simple wrapper type.
public interface RaptorServiceInputOnJob<Service : RaptorService2, Input, Output> : RaptorServiceInput2<Service, Input> {

	@RaptorDsl
	public fun after(source: RaptorServiceInput2<*, *>): RaptorServiceInputOnJob<Service, Input, Output>
}


private data class RaptorServiceInputOnJobImpl<Service : RaptorService2, Input, Output>(
	val jobId: RaptorJobId<Input, Output>,
	val dependencies: Set<RaptorServiceInput2<*, *>> = emptySet(),
) : RaptorServiceInputOnJob<Service, Input, Output> {

	override fun after(source: RaptorServiceInput2<*, *>) =
		copy(dependencies = dependencies + source)


	context(coroutineScope: CoroutineScope, context: RaptorContext, service: Service)
	override fun subscribe(handler: suspend (Input) -> Unit): Job {
		TODO("Not yet implemented")
	}
}


public interface RaptorServiceJobDefinitionBuilder<Input, Output>


internal class RaptorServiceJobDefinitionBuilderImpl<Service : RaptorService2, Input, Output>(
	internal val component: RaptorServiceComponent2<Service>,
	internal val jobId: String,
	internal val dependencies: Set<RaptorServiceInput2<*, *>> = emptySet(),
) : RaptorServiceJobDefinitionBuilder<Input, Output> {

	fun withDependency(source: RaptorServiceInput2<*, *>): RaptorServiceJobDefinitionBuilderImpl<Service, Input, Output> =
		RaptorServiceJobDefinitionBuilderImpl(component, jobId, dependencies + source)
}


internal data class JobInputSource<Service : RaptorService2, Input>(
	val jobId: String,
	val dependencies: Set<RaptorServiceInput2<*, *>> = emptySet(),
) : RaptorServiceInput2<Service, Input> {

	context(coroutineScope: CoroutineScope, context: RaptorContext, service: Service)
	override fun subscribe(handler: suspend (Input) -> Unit): Job {
		TODO("Job subscription not yet implemented.")
	}
}


@JvmName("jobUnit")
@RaptorDsl
context(component: RaptorServiceComponent2<Service>)
public fun <Service : RaptorService2> job(id: String): RaptorServiceJobDefinitionBuilder<Unit, Unit> =
	RaptorServiceJobDefinitionBuilderImpl<Service, Unit, Unit>(component, id)


@JvmName("jobInput")
@RaptorDsl
context(component: RaptorServiceComponent2<Service>)
public fun <Service : RaptorService2, Input> job(id: String): RaptorServiceJobDefinitionBuilder<Input, Unit> =
	RaptorServiceJobDefinitionBuilderImpl<Service, Input, Unit>(component, id)


@JvmName("jobInputOutput")
@RaptorDsl
context(component: RaptorServiceComponent2<Service>)
public fun <Service : RaptorService2, Input, Output> job(id: String): RaptorServiceJobDefinitionBuilder<Input, Output> =
	RaptorServiceJobDefinitionBuilderImpl<Service, Input, Output>(component, id)


@Deprecated("Not yet implemented.", level = DeprecationLevel.WARNING)
@RaptorDsl
context(component: RaptorServiceComponent2<Service>)
public fun <Service : RaptorService2, Value> RaptorServiceInput2<Service, Value>.handleAsJob(
	id: RaptorJobId<Value, *>,
) {
	// Wraps this input source so that values are enqueued as jobs rather than handled directly.
	// The job system will manage execution, retries, and status tracking.
	val jobTriggerSource = JobTriggerInputSource(source = this, jobId = id)
	component.addInputSource(jobTriggerSource) { /* Job enqueue logic handled by runtime */ }
}


@RaptorDsl
context(component: RaptorServiceComponent2<Service>)
public fun <Service : RaptorService2, Input, Output> RaptorServiceJobDefinitionBuilder<Input, Output>.handle(
	handler: suspend Service.(Input) -> Output,
): RaptorJobId<Input, Output> {
	@Suppress("UNCHECKED_CAST")
	val builder = this as RaptorServiceJobDefinitionBuilderImpl<Service, Input, Output>
	val jobInputSource = JobInputSource<Service, Input>(jobId = builder.jobId, dependencies = builder.dependencies)

	// Register the handler for the job input source.
	// The runtime will dispatch job inputs to this handler.
	component.addInputSource(jobInputSource) { input ->
		handler(input)
	}

	return RaptorJobId(builder.jobId)
}


@RaptorDsl
public fun <Input, Output> RaptorServiceJobDefinitionBuilder<Input, Output>.startAfter(
	input: RaptorServiceInput2<*, *>,
): RaptorServiceJobDefinitionBuilder<Input, Output> {
	@Suppress("UNCHECKED_CAST")
	val builder = this as RaptorServiceJobDefinitionBuilderImpl<*, Input, Output>
	return builder.withDependency(input)
}


internal data class JobTriggerInputSource<in Service : RaptorService2, Value>(
	val source: RaptorServiceInput2<Service, Value>,
	val jobId: RaptorJobId<Value, *>,
) : RaptorServiceInput2<Service, Value> {

	context(coroutineScope: CoroutineScope, context: RaptorContext, service: Service)
	override fun subscribe(handler: suspend (Value) -> Unit): Job {
		TODO("Job trigger subscription not yet implemented.")
	}
}
