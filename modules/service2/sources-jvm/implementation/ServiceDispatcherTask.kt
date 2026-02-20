package io.fluidsonic.raptor.service2


internal interface ServiceDispatcherTask {

	val id: Long
	val label: String
	val parentId: Long
	val rootId: Long
	val type: ServiceDispatcher.TaskType
}
