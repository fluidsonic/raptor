package io.fluidsonic.raptor

import org.bson.types.*


internal class RaptorEntityBsonIdFactory<out Id : RaptorEntityId>(private val wrap: (ObjectId) -> Id) : RaptorEntityIdFactory<Id> {

	override fun create(): Id =
		wrap(ObjectId())
}


@Suppress("FunctionName")
public fun <Id : RaptorEntityId> RaptorEntityBsonIdFactory(wrap: (String) -> Id): RaptorEntityIdFactory<Id> =
	RaptorEntityBsonIdFactory { id: ObjectId -> wrap(id.toHexString()) }
