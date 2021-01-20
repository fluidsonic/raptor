package io.fluidsonic.raptor


public interface RaptorJobRegistry {

	public operator fun get(id: String): RaptorJobExecutor<*>?
}


public fun RaptorJobRegistry(executors: Collection<RaptorJobExecutor<*>>): RaptorJobRegistry =
	RaptorJobRegistryImpl(executors = executors)
