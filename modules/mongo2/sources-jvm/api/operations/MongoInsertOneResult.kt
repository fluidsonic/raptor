package io.fluidsonic.raptor.mongo2

import com.mongodb.client.result.*
import org.bson.*


public data class MongoInsertOneResult(
	val insertedId: BsonValue?,
) {

	public companion object {

		internal fun fromSource(result: InsertOneResult) =
			result.takeIf { it.wasAcknowledged() }?.let { ack ->
				MongoInsertOneResult(
					insertedId = ack.insertedId,
				)
			}
	}
}
