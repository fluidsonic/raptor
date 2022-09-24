package io.fluidsonic.raptor

import io.fluidsonic.raptor.di.*
import kotlin.collections.set
import kotlin.reflect.*


public class RaptorJobsComponent internal constructor() : RaptorComponent.Base<RaptorJobsComponent>() {

	private val executorsById: MutableMap<String, RaptorJobExecutor<*>> = hashMapOf()


	internal fun complete(): RaptorJobRegistry =
		RaptorJobRegistry(executorsById.values)


	@RaptorDsl
	public fun <Data> register(executor: RaptorJobExecutor<Data>) {
		val id = executor.group.id
		check(!executorsById.containsKey(id)) { "Cannot register multiple job executors with same ID: $id" }

		executorsById[id] = executor
	}
}


@RaptorDsl
public inline fun <Data, reified Dependency> RaptorAssemblyQuery<RaptorJobsComponent>.register(
	group: RaptorJobGroup<Data>,
	noinline execute: suspend Dependency.(data: Data) -> Unit,
) {
	register(group = group, dependencyType = typeOf<Dependency>(), execute = execute)
}


@RaptorDsl
@Suppress("UNCHECKED_CAST")
public fun <Data, Dependency> RaptorAssemblyQuery<RaptorJobsComponent>.register(
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
public fun <Data> RaptorAssemblyQuery<RaptorJobsComponent>.register(executor: RaptorJobExecutor<Data>) {
	this { register(executor) }
}
