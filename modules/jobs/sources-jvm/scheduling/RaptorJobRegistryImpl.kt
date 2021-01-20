package io.fluidsonic.raptor


internal class RaptorJobRegistryImpl(executors: Collection<RaptorJobExecutor<*>>) : RaptorJobRegistry {

	private val executorsById = executors.associateBy { it.group.id }


	override fun get(id: String) =
		executorsById[id]
}
