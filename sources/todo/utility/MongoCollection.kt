package io.fluidsonic.raptor

import com.mongodb.client.model.*
import com.mongodb.client.model.Filters.*
import io.fluidsonic.mongo.*
import io.fluidsonic.stdlib.*
import org.bson.*
import org.bson.conversions.*
import kotlin.reflect.*


// FIXME move to fluid-mongo?

suspend fun MongoCollection<*>.deleteOneById(id: Any?, options: DeleteOptions = DeleteOptions()) =
	deleteOne(filter = eq("_id", id), options = options)


suspend fun <TDocument : Any> MongoCollection<TDocument>.findOneById(id: Any?) =
	find(filter = eq("_id", id)).firstOrNull()


suspend fun <TResult : Any> MongoCollection<*>.findOneById(id: Any?, resultClass: KClass<out TResult>) =
	find(filter = eq("_id", id), resultClass = resultClass).firstOrNull()


suspend fun <TResult : Any> MongoCollection<*>.findOneFieldById(id: Any?, fieldName: String, resultClass: KClass<out TResult>): TResult? =
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
			document.asBsonReader().readDocument {
				(readBsonType() != BsonType.END_OF_DOCUMENT).thenTake {
					check(readName() == fieldName)

					codecRegistry.get(resultClass.java).decode(this, RaptorBsonScope.decoderContext)
				}
			}
		}


suspend fun <TDocument : Any> MongoCollection<TDocument>.findOneByIdAndDelete(
	id: Any?,
	options: FindOneAndDeleteOptions = FindOneAndDeleteOptions()
) =
	findOneAndDelete(
		filter = eq("_id", id),
		options = options
	)


suspend fun <TDocument : Any> MongoCollection<TDocument>.findOneByIdAndUpdate(
	id: Any?,
	update: Bson,
	options: FindOneAndUpdateOptions = FindOneAndUpdateOptions()
) =
	findOneAndUpdate(
		filter = eq("_id", id),
		update = update,
		options = options
	)


suspend fun <TDocument : Any> MongoCollection<TDocument>.replaceOneById(id: Any?, replacement: TDocument, options: ReplaceOptions = ReplaceOptions()) =
	replaceOne(filter = eq("_id", id), replacement = replacement, options = options)


suspend fun <TDocument : Any> MongoCollection<TDocument>.updateOneById(id: Any?, update: Bson, options: UpdateOptions = UpdateOptions()) =
	updateOne(filter = eq("_id", id), update = update, options = options)
