package io.fluidsonic.raptor.mongo2

import com.mongodb.client.result.*
import org.bson.*


public data class MongoInsertManyResult(
	val insertedIds: Map<Int, BsonValue>,
) {

	public companion object {

		internal fun fromSource(result: InsertManyResult) =
			result.takeIf { it.wasAcknowledged() }?.let { ack ->
				MongoInsertManyResult(
					insertedIds = ack.insertedIds,
				)
			}
	}
}
