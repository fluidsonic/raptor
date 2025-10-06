package io.fluidsonic.raptor.mongo2

import com.mongodb.client.result.*


public data class MongoDeleteResult(
	val deletedCount: Long,
) {

	public companion object {

		internal fun fromSource(result: DeleteResult) =
			result.takeIf { it.wasAcknowledged() }?.let { ack ->
				MongoDeleteResult(
					deletedCount = ack.deletedCount,
				)
			}
	}
}
