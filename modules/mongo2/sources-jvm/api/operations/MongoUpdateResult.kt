package io.fluidsonic.raptor.mongo2

import com.mongodb.client.result.*
import org.bson.*


public data class MongoUpdateResult(
	val matchedCount: Long,
	val modifiedCount: Long,
	val upsertedId: BsonValue?,
) {

	public companion object {

		internal fun fromSource(result: UpdateResult) =
			result.takeIf { it.wasAcknowledged() }?.let { ack ->
				MongoUpdateResult(
					matchedCount = ack.matchedCount,
					modifiedCount = ack.modifiedCount,
					upsertedId = ack.upsertedId,
				)
			}
	}
}
