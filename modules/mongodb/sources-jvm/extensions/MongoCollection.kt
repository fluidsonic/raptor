package io.fluidsonic.raptor.mongo

import com.mongodb.client.model.*
import com.mongodb.client.model.Filters.*
import com.mongodb.client.result.*
import io.fluidsonic.mongo.*
import io.fluidsonic.stdlib.*
import kotlin.reflect.*
import org.bson.*
import org.bson.codecs.*
import org.bson.conversions.*


private val bsonDecoderContext = DecoderContext.builder().build()!!

// TODO move to fluid-mongo?

public suspend fun MongoCollection<*>.deleteOneById(id: Any?, options: DeleteOptions = DeleteOptions()): DeleteResult =
	deleteOne(filter = eq("_id", id), options = options)


public fun <TDocument : Any> MongoCollection<TDocument>.findById(ids: Iterable<*>): FindFlow<TDocument> =
	findById(ids = ids, resultClass = documentClass)


public fun <TResult : Any> MongoCollection<*>.findById(ids: Iterable<*>, resultClass: KClass<out TResult>): FindFlow<TResult> {
	val collection = (ids as? Collection<*>) ?: ids.toList()
	if (collection.isEmpty())
		return FindFlow.empty()

	return find(filter = `in`("_id", collection), resultClass = resultClass)
}


public suspend fun <TDocument : Any> MongoCollection<TDocument>.findOneById(id: Any?): TDocument? =
	findOneById(id = id, resultClass = documentClass)


public suspend fun <TResult : Any> MongoCollection<*>.findOneById(id: Any?, resultClass: KClass<out TResult>): TResult? =
	find(filter = eq("_id", id), resultClass = resultClass).firstOrNull()


public suspend inline fun <reified TResult : Any> MongoCollection<*>.findOneFieldById(id: Any?, fieldName: String): TResult? =
	findOneFieldById(id, fieldName = fieldName, resultClass = TResult::class)


public suspend fun <TResult : Any> MongoCollection<*>.findOneFieldById(id: Any?, fieldName: String, resultClass: KClass<out TResult>): TResult? =
	find(
		filter = eq("_id", id),
		resultClass = RawBsonDocument::class
	)
		.projection(
			if (fieldName == "_id") Projections.include(fieldName)
			else Projections.fields(Projections.include(fieldName), Projections.excludeId())
		)
		.firstOrNull()
		?.let { document ->
			with(document.asBsonReader()) {
				readStartDocument()

				val result = (readBsonType() != BsonType.END_OF_DOCUMENT).thenTake {
					check(readName() == fieldName)

					codecRegistry.get(resultClass.java).decode(this, bsonDecoderContext)
				}

				readEndDocument()

				result
			}
		}


public suspend fun <TDocument : Any> MongoCollection<TDocument>.findOneByIdAndDelete(
	id: Any?,
	options: FindOneAndDeleteOptions = FindOneAndDeleteOptions(),
): TDocument? =
	findOneAndDelete(
		filter = eq("_id", id),
		options = options
	)


public suspend fun <TDocument : Any> MongoCollection<TDocument>.findOneByIdAndUpdate(
	id: Any?,
	update: Bson,
	options: FindOneAndUpdateOptions = FindOneAndUpdateOptions(),
): TDocument? =
	findOneAndUpdate(
		filter = eq("_id", id),
		update = update,
		options = options
	)


public suspend fun <TDocument : Any> MongoCollection<TDocument>.replaceOneById(
	id: Any?,
	replacement: TDocument,
	options: ReplaceOptions = ReplaceOptions(),
): UpdateResult =
	replaceOne(filter = eq("_id", id), replacement = replacement, options = options)


public suspend fun <TDocument : Any> MongoCollection<TDocument>.updateOneById(
	id: Any?,
	update: Bson,
	options: UpdateOptions = UpdateOptions(),
): UpdateResult =
	updateOne(filter = eq("_id", id), update = update, options = options)
