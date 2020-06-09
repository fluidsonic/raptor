package io.fluidsonic.raptor

import com.mongodb.client.model.*
import io.fluidsonic.mongo.*
import org.bson.conversions.*


internal class RaptorMongoUpdate(
	val filter: Bson,
	val changes: List<Bson>,
	val upsert: Boolean
)


@PublishedApi
internal suspend fun <Document : Any> MongoCollection<Document>.execute(update: RaptorMongoUpdate) =
	if (update.changes.isEmpty() && !update.upsert)
		find(filter = update.filter).firstOrNull()
	else
		findOneAndUpdate(
			filter = update.filter,
			update = Updates.combine(update.changes),
			options = FindOneAndUpdateOptions().upsert(update.upsert).returnDocument(ReturnDocument.AFTER)
		)


@RaptorDsl
suspend inline fun <Document : Any> MongoCollection<Document>.updateOne(configure: RaptorMongoUpdateBuilder.() -> Unit) =
	execute(RaptorMongoUpdateBuilder().apply(configure).build())


@RaptorDsl
suspend fun <Document : Any> MongoCollection<Document>.updateOneById(id: Any?, configure: RaptorMongoUpdateBuilder.() -> Unit) =
	updateOne {
		filter(Filters.eq("_id", id))

		configure()
	}


@RaptorDsl
suspend inline fun <Document : Any> MongoCollection<Document>.upsertOne(configure: RaptorMongoUpdateBuilder.() -> Unit) =
	execute(RaptorMongoUpdateBuilder(upsert = true).apply(configure).build())!!
