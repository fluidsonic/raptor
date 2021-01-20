package io.fluidsonic.raptor

import kotlin.collections.set
import kotlin.reflect.*


public class RaptorJobsComponent internal constructor() : RaptorComponent.Default<RaptorJobsComponent>() {

	private val executorsById: MutableMap<String, RaptorJobExecutor<*>> = hashMapOf()


	internal fun createRegistry(): RaptorJobRegistry =
		RaptorJobRegistry(executorsById.values)


	@RaptorDsl
	@Suppress("UNCHECKED_CAST")
	public fun <Data> register(executor: RaptorJobExecutor<Data>) {
		val id = executor.group.id
		check(!executorsById.containsKey(id)) { "Cannot register multiple job executors with same ID: $id" }

		executorsById[id] = executor
	}


	public companion object;


	internal object Key : RaptorComponentKey<RaptorJobsComponent> {

		override fun toString() = "jobs"
	}
}


@OptIn(ExperimentalStdlibApi::class)
@RaptorDsl
public inline fun <Data, reified Dependency> RaptorComponentSet<RaptorJobsComponent>.register(
	group: RaptorJobGroup<Data>,
	noinline execute: suspend Dependency.(data: Data) -> Unit,
) {
	register(group = group, dependencyType = typeOf<Dependency>(), execute = execute)
}


@RaptorDsl
@Suppress("UNCHECKED_CAST")
public fun <Data, Dependency> RaptorComponentSet<RaptorJobsComponent>.register(
	group: RaptorJobGroup<Data>,
	dependencyType: KType,
	execute: suspend Dependency.(data: Data) -> Unit,
) {
	register(group.executor { data ->
		val dependency = di.get(dependencyType) as Dependency
		dependency.execute(data)
	})
}


@RaptorDsl
public fun <Data> RaptorComponentSet<RaptorJobsComponent>.register(executor: RaptorJobExecutor<Data>) {
	configure { register(executor) }
}
