@file:Suppress("UNCHECKED_CAST")

package tests.utility

import com.mongodb.*
import com.mongodb.bulk.*
import com.mongodb.client.model.*
import com.mongodb.client.result.*
import io.fluidsonic.mongo.*
import kotlinx.coroutines.flow.*
import org.bson.*
import org.bson.codecs.*
import org.bson.codecs.configuration.*
import org.bson.conversions.*
import kotlin.reflect.*


internal class TestMongoCollection<TDocument : Any>(
	override val namespace: MongoNamespace,
	override val documentClass: KClass<TDocument>,
	override val codecRegistry: CodecRegistry,
	override val readPreference: ReadPreference = ReadPreference.primary(),
	override val writeConcern: WriteConcern = WriteConcern.ACKNOWLEDGED,
	override val readConcern: ReadConcern = ReadConcern.DEFAULT,
	private val data: MutableMap<BsonValue, BsonDocument>,
) : MongoCollection<TDocument> {

	private fun <T : Any> encodeToBson(value: T, codec: Codec<T>): BsonDocument {
		val document = BsonDocument()
		val writer = BsonDocumentWriter(document)
		codec.encode(writer, value, EncoderContext.builder().build())
		return document
	}

	private fun extractIdFromFilter(filter: Bson): BsonValue? {
		val filterDoc = filter.toBsonDocument(BsonDocument::class.java, codecRegistry)
		return filterDoc["_id"]
	}


	// -- withX methods --

	override fun <NewTDocument : Any> withDocumentClass(newDocumentClass: KClass<NewTDocument>): MongoCollection<NewTDocument> =
		TestMongoCollection(
			namespace = namespace,
			documentClass = newDocumentClass,
			codecRegistry = codecRegistry,
			readPreference = readPreference,
			writeConcern = writeConcern,
			readConcern = readConcern,
			data = data,
		)

	override fun withCodecRegistry(codecRegistry: CodecRegistry): MongoCollection<TDocument> =
		TestMongoCollection(
			namespace = namespace,
			documentClass = documentClass,
			codecRegistry = codecRegistry,
			readPreference = readPreference,
			writeConcern = writeConcern,
			readConcern = readConcern,
			data = data,
		)

	override fun withReadPreference(readPreference: ReadPreference): MongoCollection<TDocument> =
		TestMongoCollection(
			namespace = namespace,
			documentClass = documentClass,
			codecRegistry = codecRegistry,
			readPreference = readPreference,
			writeConcern = writeConcern,
			readConcern = readConcern,
			data = data,
		)

	override fun withWriteConcern(writeConcern: WriteConcern): MongoCollection<TDocument> =
		TestMongoCollection(
			namespace = namespace,
			documentClass = documentClass,
			codecRegistry = codecRegistry,
			readPreference = readPreference,
			writeConcern = writeConcern,
			readConcern = readConcern,
			data = data,
		)

	override fun withReadConcern(readConcern: ReadConcern): MongoCollection<TDocument> =
		TestMongoCollection(
			namespace = namespace,
			documentClass = documentClass,
			codecRegistry = codecRegistry,
			readPreference = readPreference,
			writeConcern = writeConcern,
			readConcern = readConcern,
			data = data,
		)


	// -- find methods --

	override fun find(): FindFlow<TDocument> =
		find(resultClass = documentClass)

	override fun <TResult : Any> find(resultClass: KClass<out TResult>): FindFlow<TResult> =
		TestFindFlow(
			documents = data.values.toList(),
			codecRegistry = codecRegistry,
			resultClass = resultClass,
		)

	override fun find(filter: Bson): FindFlow<TDocument> =
		find(filter = filter, resultClass = documentClass)

	override fun <TResult : Any> find(filter: Bson, resultClass: KClass<out TResult>): FindFlow<TResult> =
		TestFindFlow(
			documents = data.values.toList(),
			codecRegistry = codecRegistry,
			resultClass = resultClass,
			filterBson = filter,
		)

	override fun find(clientSession: ClientSession): FindFlow<TDocument> = find()
	override fun <TResult : Any> find(clientSession: ClientSession, resultClass: KClass<out TResult>): FindFlow<TResult> = find(resultClass = resultClass)
	override fun find(clientSession: ClientSession, filter: Bson): FindFlow<TDocument> = find(filter = filter)
	override fun <TResult : Any> find(clientSession: ClientSession, filter: Bson, resultClass: KClass<out TResult>): FindFlow<TResult> = find(filter = filter, resultClass = resultClass)


	// -- deleteMany --

	override suspend fun deleteMany(filter: Bson): DeleteResult {
		val filterDoc = filter.toBsonDocument(BsonDocument::class.java, codecRegistry)
		if (filterDoc.isEmpty()) {
			val count = data.size.toLong()
			data.clear()
			return DeleteResult.acknowledged(count)
		}

		val id = extractIdFromFilter(filter)
		if (id != null) {
			val removed = data.remove(id)
			return DeleteResult.acknowledged(if (removed != null) 1L else 0L)
		}

		return DeleteResult.acknowledged(0)
	}

	override suspend fun deleteMany(filter: Bson, options: DeleteOptions): DeleteResult = deleteMany(filter)
	override suspend fun deleteMany(clientSession: ClientSession, filter: Bson): DeleteResult = deleteMany(filter)
	override suspend fun deleteMany(clientSession: ClientSession, filter: Bson, options: DeleteOptions): DeleteResult = deleteMany(filter)


	// -- deleteOne --

	override suspend fun deleteOne(filter: Bson): DeleteResult {
		val id = extractIdFromFilter(filter) ?: return DeleteResult.acknowledged(0)
		val removed = data.remove(id)
		return DeleteResult.acknowledged(if (removed != null) 1L else 0L)
	}

	override suspend fun deleteOne(filter: Bson, options: DeleteOptions): DeleteResult = deleteOne(filter)
	override suspend fun deleteOne(clientSession: ClientSession, filter: Bson): DeleteResult = deleteOne(filter)
	override suspend fun deleteOne(clientSession: ClientSession, filter: Bson, options: DeleteOptions): DeleteResult = deleteOne(filter)


	// -- replaceOne --

	override suspend fun replaceOne(filter: Bson, replacement: TDocument): UpdateResult =
		replaceOne(filter, replacement, ReplaceOptions())

	override suspend fun replaceOne(filter: Bson, replacement: TDocument, options: ReplaceOptions): UpdateResult {
		val id = extractIdFromFilter(filter) ?: return UpdateResult.acknowledged(0, 0, null)

		val codec = codecRegistry.get(documentClass.java) as Codec<TDocument>
		val bsonDoc = encodeToBson(replacement, codec)

		val existed = data.containsKey(id)
		data[id] = bsonDoc

		return if (existed) {
			UpdateResult.acknowledged(1, 1, null)
		} else if (options.isUpsert) {
			UpdateResult.acknowledged(0, 0, id)
		} else {
			UpdateResult.acknowledged(0, 0, null)
		}
	}

	override suspend fun replaceOne(clientSession: ClientSession, filter: Bson, replacement: TDocument): UpdateResult =
		replaceOne(filter, replacement)

	override suspend fun replaceOne(clientSession: ClientSession, filter: Bson, replacement: TDocument, options: ReplaceOptions): UpdateResult =
		replaceOne(filter, replacement, options)


	// -- updateOne --

	override suspend fun updateOne(filter: Bson, update: Bson): UpdateResult =
		updateOne(filter, update, UpdateOptions())

	override suspend fun updateOne(filter: Bson, update: Bson, options: UpdateOptions): UpdateResult {
		val id = extractIdFromFilter(filter) ?: return UpdateResult.acknowledged(0, 0, null)

		val existed = data.containsKey(id)

		if (existed) {
			// For $setOnInsert, do nothing if document exists.
			return UpdateResult.acknowledged(1, 0, null)
		}

		if (!options.isUpsert) return UpdateResult.acknowledged(0, 0, null)

		// Upsert: create new document from _id + $setOnInsert fields
		val updateDoc = update.toBsonDocument(BsonDocument::class.java, codecRegistry)
		val setOnInsertDoc = updateDoc.getDocument("\$setOnInsert", null)

		val newDoc = BsonDocument()
		newDoc.append("_id", id)
		if (setOnInsertDoc != null) {
			for (key in setOnInsertDoc.keys) {
				newDoc.append(key, setOnInsertDoc[key])
			}
		}

		data[id] = newDoc
		return UpdateResult.acknowledged(0, 0, id)
	}

	override suspend fun updateOne(clientSession: ClientSession, filter: Bson, update: Bson): UpdateResult = updateOne(filter, update)
	override suspend fun updateOne(clientSession: ClientSession, filter: Bson, update: Bson, options: UpdateOptions): UpdateResult = updateOne(filter, update, options)


	// -- Not implemented methods --

	override suspend fun estimatedDocumentCount(): Long = error("not implemented")
	override suspend fun estimatedDocumentCount(options: EstimatedDocumentCountOptions): Long = error("not implemented")
	override suspend fun countDocuments(): Long = error("not implemented")
	override suspend fun countDocuments(filter: Bson): Long = error("not implemented")
	override suspend fun countDocuments(filter: Bson, options: CountOptions): Long = error("not implemented")
	override suspend fun countDocuments(clientSession: ClientSession): Long = error("not implemented")
	override suspend fun countDocuments(clientSession: ClientSession, filter: Bson): Long = error("not implemented")
	override suspend fun countDocuments(clientSession: ClientSession, filter: Bson, options: CountOptions): Long = error("not implemented")

	override fun aggregate(pipeline: List<Bson>): AggregateFlow<TDocument> = error("not implemented")
	override fun <TResult : Any> aggregate(pipeline: List<Bson>, resultClass: KClass<out TResult>): AggregateFlow<TResult> = error("not implemented")
	override fun aggregate(clientSession: ClientSession, pipeline: List<Bson>): AggregateFlow<TDocument> = error("not implemented")
	override fun <TResult : Any> aggregate(clientSession: ClientSession, pipeline: List<Bson>, resultClass: KClass<out TResult>): AggregateFlow<TResult> = error("not implemented")

	override fun <TResult : Any> distinct(fieldName: String, resultClass: KClass<out TResult>): DistinctFlow<TResult> = error("not implemented")
	override fun <TResult : Any> distinct(fieldName: String, filter: Bson, resultClass: KClass<out TResult>): DistinctFlow<TResult> = error("not implemented")
	override fun <TResult : Any> distinct(clientSession: ClientSession, fieldName: String, resultClass: KClass<out TResult>): DistinctFlow<TResult> = error("not implemented")
	override fun <TResult : Any> distinct(clientSession: ClientSession, fieldName: String, filter: Bson, resultClass: KClass<out TResult>): DistinctFlow<TResult> = error("not implemented")

	override fun watch(): ChangeStreamFlow<Document> = error("not implemented")
	override fun <TResult : Any> watch(resultClass: KClass<out TResult>): ChangeStreamFlow<TResult> = error("not implemented")
	override fun watch(pipeline: List<Bson>): ChangeStreamFlow<Document> = error("not implemented")
	override fun <TResult : Any> watch(pipeline: List<Bson>, resultClass: KClass<out TResult>): ChangeStreamFlow<TResult> = error("not implemented")
	override fun watch(clientSession: ClientSession): ChangeStreamFlow<Document> = error("not implemented")
	override fun <TResult : Any> watch(clientSession: ClientSession, resultClass: KClass<out TResult>): ChangeStreamFlow<TResult> = error("not implemented")
	override fun watch(clientSession: ClientSession, pipeline: List<Bson>): ChangeStreamFlow<Document> = error("not implemented")
	override fun <TResult : Any> watch(clientSession: ClientSession, pipeline: List<Bson>, resultClass: KClass<out TResult>): ChangeStreamFlow<TResult> = error("not implemented")

	override fun mapReduce(mapFunction: String, reduceFunction: String): MapReduceFlow<TDocument> = error("not implemented")
	override fun <TResult : Any> mapReduce(mapFunction: String, reduceFunction: String, resultClass: KClass<out TResult>): MapReduceFlow<TResult> = error("not implemented")
	override fun mapReduce(clientSession: ClientSession, mapFunction: String, reduceFunction: String): MapReduceFlow<TDocument> = error("not implemented")
	override fun <TResult : Any> mapReduce(clientSession: ClientSession, mapFunction: String, reduceFunction: String, resultClass: KClass<out TResult>): MapReduceFlow<TResult> = error("not implemented")

	override suspend fun insertOne(document: TDocument): InsertOneResult =
		insertOne(document, InsertOneOptions())

	override suspend fun insertOne(document: TDocument, options: InsertOneOptions): InsertOneResult {
		val codec = codecRegistry.get(documentClass.java) as Codec<TDocument>
		val bsonDoc = encodeToBson(document, codec)
		val id = bsonDoc["_id"] ?: BsonObjectId()
		if (!bsonDoc.containsKey("_id")) bsonDoc.append("_id", id)
		data[id] = bsonDoc
		return InsertOneResult.acknowledged(id)
	}

	override suspend fun insertOne(clientSession: ClientSession, document: TDocument): InsertOneResult =
		insertOne(document)

	override suspend fun insertOne(clientSession: ClientSession, document: TDocument, options: InsertOneOptions): InsertOneResult =
		insertOne(document, options)
	override suspend fun insertMany(documents: List<TDocument>): InsertManyResult = error("not implemented")
	override suspend fun insertMany(documents: List<TDocument>, options: InsertManyOptions): InsertManyResult = error("not implemented")
	override suspend fun insertMany(clientSession: ClientSession, documents: List<TDocument>): InsertManyResult = error("not implemented")
	override suspend fun insertMany(clientSession: ClientSession, documents: List<TDocument>, options: InsertManyOptions): InsertManyResult = error("not implemented")

	override suspend fun updateMany(filter: Bson, update: Bson): UpdateResult = error("not implemented")
	override suspend fun updateMany(filter: Bson, update: Bson, options: UpdateOptions): UpdateResult = error("not implemented")
	override suspend fun updateMany(clientSession: ClientSession, filter: Bson, update: Bson): UpdateResult = error("not implemented")
	override suspend fun updateMany(clientSession: ClientSession, filter: Bson, update: Bson, options: UpdateOptions): UpdateResult = error("not implemented")

	override suspend fun findOneAndDelete(filter: Bson): TDocument? = error("not implemented")
	override suspend fun findOneAndDelete(filter: Bson, options: FindOneAndDeleteOptions): TDocument? = error("not implemented")
	override suspend fun findOneAndDelete(clientSession: ClientSession, filter: Bson): TDocument? = error("not implemented")
	override suspend fun findOneAndDelete(clientSession: ClientSession, filter: Bson, options: FindOneAndDeleteOptions): TDocument? = error("not implemented")
	override suspend fun findOneAndReplace(filter: Bson, replacement: TDocument): TDocument? = error("not implemented")
	override suspend fun findOneAndReplace(filter: Bson, replacement: TDocument, options: FindOneAndReplaceOptions): TDocument? = error("not implemented")
	override suspend fun findOneAndReplace(clientSession: ClientSession, filter: Bson, replacement: TDocument): TDocument? = error("not implemented")
	override suspend fun findOneAndReplace(clientSession: ClientSession, filter: Bson, replacement: TDocument, options: FindOneAndReplaceOptions): TDocument? = error("not implemented")
	override suspend fun findOneAndUpdate(filter: Bson, update: Bson): TDocument? = error("not implemented")
	override suspend fun findOneAndUpdate(filter: Bson, update: Bson, options: FindOneAndUpdateOptions): TDocument? = error("not implemented")
	override suspend fun findOneAndUpdate(clientSession: ClientSession, filter: Bson, update: Bson): TDocument? = error("not implemented")
	override suspend fun findOneAndUpdate(clientSession: ClientSession, filter: Bson, update: Bson, options: FindOneAndUpdateOptions): TDocument? = error("not implemented")

	override suspend fun bulkWrite(requests: List<WriteModel<out TDocument>>): BulkWriteResult = error("not implemented")
	override suspend fun bulkWrite(requests: List<WriteModel<out TDocument>>, options: BulkWriteOptions): BulkWriteResult = error("not implemented")
	override suspend fun bulkWrite(clientSession: ClientSession, requests: List<WriteModel<out TDocument>>): BulkWriteResult = error("not implemented")
	override suspend fun bulkWrite(clientSession: ClientSession, requests: List<WriteModel<out TDocument>>, options: BulkWriteOptions): BulkWriteResult = error("not implemented")

	override suspend fun createIndex(key: Bson): String = error("not implemented")
	override suspend fun createIndex(key: Bson, options: IndexOptions): String = error("not implemented")
	override suspend fun createIndex(clientSession: ClientSession, key: Bson): String = error("not implemented")
	override suspend fun createIndex(clientSession: ClientSession, key: Bson, options: IndexOptions): String = error("not implemented")
	override suspend fun createIndexes(indexes: List<IndexModel>): kotlinx.coroutines.flow.Flow<String> = error("not implemented")
	override suspend fun createIndexes(indexes: List<IndexModel>, createIndexOptions: CreateIndexOptions): kotlinx.coroutines.flow.Flow<String> = error("not implemented")
	override suspend fun createIndexes(clientSession: ClientSession, indexes: List<IndexModel>): kotlinx.coroutines.flow.Flow<String> = error("not implemented")
	override suspend fun createIndexes(clientSession: ClientSession, indexes: List<IndexModel>, createIndexOptions: CreateIndexOptions): kotlinx.coroutines.flow.Flow<String> = error("not implemented")
	override fun listIndexes(): ListIndexesFlow<Document> = error("not implemented")
	override fun <TResult : Any> listIndexes(resultClass: KClass<out TResult>): ListIndexesFlow<TResult> = error("not implemented")
	override fun listIndexes(clientSession: ClientSession): ListIndexesFlow<Document> = error("not implemented")
	override fun <TResult : Any> listIndexes(clientSession: ClientSession, resultClass: KClass<out TResult>): ListIndexesFlow<TResult> = error("not implemented")
	override suspend fun dropIndex(indexName: String): Unit = error("not implemented")
	override suspend fun dropIndex(indexName: String, dropIndexOptions: DropIndexOptions): Unit = error("not implemented")
	override suspend fun dropIndex(keys: Bson): Unit = error("not implemented")
	override suspend fun dropIndex(keys: Bson, dropIndexOptions: DropIndexOptions): Unit = error("not implemented")
	override suspend fun dropIndex(clientSession: ClientSession, indexName: String): Unit = error("not implemented")
	override suspend fun dropIndex(clientSession: ClientSession, indexName: String, dropIndexOptions: DropIndexOptions): Unit = error("not implemented")
	override suspend fun dropIndex(clientSession: ClientSession, keys: Bson): Unit = error("not implemented")
	override suspend fun dropIndex(clientSession: ClientSession, keys: Bson, dropIndexOptions: DropIndexOptions): Unit = error("not implemented")
	override suspend fun dropIndexes(): Unit = error("not implemented")
	override suspend fun dropIndexes(dropIndexOptions: DropIndexOptions): Unit = error("not implemented")
	override suspend fun dropIndexes(clientSession: ClientSession): Unit = error("not implemented")
	override suspend fun dropIndexes(clientSession: ClientSession, dropIndexOptions: DropIndexOptions): Unit = error("not implemented")

	override suspend fun drop(): Unit = error("not implemented")
	override suspend fun drop(clientSession: ClientSession): Unit = error("not implemented")
	override suspend fun renameCollection(newCollectionNamespace: MongoNamespace): Unit = error("not implemented")
	override suspend fun renameCollection(newCollectionNamespace: MongoNamespace, options: RenameCollectionOptions): Unit = error("not implemented")
	override suspend fun renameCollection(clientSession: ClientSession, newCollectionNamespace: MongoNamespace): Unit = error("not implemented")
	override suspend fun renameCollection(clientSession: ClientSession, newCollectionNamespace: MongoNamespace, options: RenameCollectionOptions): Unit = error("not implemented")
}
