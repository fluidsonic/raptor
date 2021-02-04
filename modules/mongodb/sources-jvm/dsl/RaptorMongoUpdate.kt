package io.fluidsonic.raptor

import com.mongodb.client.model.*
import io.fluidsonic.mongo.*
import org.bson.*
import org.bson.conversions.*


internal class RaptorMongoUpdate(
	val filter: Bson?,
	val changes: Bson?,
	val isUpsert: Boolean,
)


@PublishedApi
internal suspend fun <Document : Any> MongoCollection<Document>.execute(update: RaptorMongoUpdate): Document? {
	val filter = update.filter ?: BsonDocument()
	val changes = update.changes ?: return find(filter = filter).firstOrNull()

	return findOneAndUpdate(
		filter = filter,
		update = changes,
		options = FindOneAndUpdateOptions().upsert(update.isUpsert).returnDocument(ReturnDocument.AFTER)
	)
}


public suspend inline fun <Document : Any> MongoCollection<Document>.updateOne(configure: RaptorMongodbUpdateBuilder.() -> Unit): Document? =
	execute(RaptorMongodbUpdateBuilder().apply(configure).build())


public suspend fun <Document : Any> MongoCollection<Document>.updateOneById(id: Any?, configure: RaptorMongodbUpdateBuilder.() -> Unit): Document? =
	updateOne {
		filter { id(id) }

		configure()
	}


public suspend inline fun <Document : Any> MongoCollection<Document>.upsertOne(configure: RaptorMongodbUpdateBuilder.() -> Unit): Document =
	execute(RaptorMongodbUpdateBuilder(isUpsert = true).apply(configure).build())!!


public suspend fun <Document : Any> MongoCollection<Document>.upsertOneById(id: Any?, configure: RaptorMongodbUpdateBuilder.() -> Unit): Document? =
	upsertOne {
		filter { id(id) }

		configure()
	}
