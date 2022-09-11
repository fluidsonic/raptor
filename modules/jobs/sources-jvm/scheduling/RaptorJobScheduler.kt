package io.fluidsonic.raptor

import io.fluidsonic.raptor.di.*


public interface RaptorJobScheduler {

	public suspend fun queryStatus(id: String, group: RaptorJobGroup<*>): RaptorJobStatus?
	public suspend fun remove(id: String, group: RaptorJobGroup<*>)
	public suspend fun <Data> schedule(id: String, group: RaptorJobGroup<Data>, data: Data, timing: RaptorJobTiming)
}


public val RaptorScope.jobScheduler: RaptorJobScheduler
	get() = di.get()
