package tests.utility

import com.mongodb.*
import com.mongodb.client.model.*
import io.fluidsonic.mongo.*
import org.bson.*
import org.bson.codecs.configuration.*
import org.bson.conversions.*
import kotlin.reflect.*


internal class TestMongoDatabase(
	override val name: String = "test",
	override val codecRegistry: CodecRegistry = MongoClientSettings.getDefaultCodecRegistry(),
	override val readPreference: ReadPreference = ReadPreference.primary(),
	override val writeConcern: WriteConcern = WriteConcern.ACKNOWLEDGED,
	override val readConcern: ReadConcern = ReadConcern.DEFAULT,
) : MongoDatabase {

	private val collections = hashMapOf<String, MutableMap<BsonValue, BsonDocument>>()

	override fun withCodecRegistry(codecRegistry: CodecRegistry): MongoDatabase =
		TestMongoDatabase(
			name = name,
			codecRegistry = codecRegistry,
			readPreference = readPreference,
			writeConcern = writeConcern,
			readConcern = readConcern,
		)

	override fun withReadPreference(readPreference: ReadPreference): MongoDatabase =
		TestMongoDatabase(
			name = name,
			codecRegistry = codecRegistry,
			readPreference = readPreference,
			writeConcern = writeConcern,
			readConcern = readConcern,
		)

	override fun withWriteConcern(writeConcern: WriteConcern): MongoDatabase =
		TestMongoDatabase(
			name = name,
			codecRegistry = codecRegistry,
			readPreference = readPreference,
			writeConcern = writeConcern,
			readConcern = readConcern,
		)

	override fun withReadConcern(readConcern: ReadConcern): MongoDatabase =
		TestMongoDatabase(
			name = name,
			codecRegistry = codecRegistry,
			readPreference = readPreference,
			writeConcern = writeConcern,
			readConcern = readConcern,
		)

	override fun getCollection(name: String): MongoCollection<Document> =
		getCollection(name, Document::class)

	override fun <TDocument : Any> getCollection(name: String, documentClass: KClass<TDocument>): MongoCollection<TDocument> =
		TestMongoCollection(
			namespace = MongoNamespace(this.name, name),
			documentClass = documentClass,
			codecRegistry = codecRegistry,
			readPreference = readPreference,
			writeConcern = writeConcern,
			readConcern = readConcern,
			data = collections.getOrPut(name) { linkedMapOf() },
		)


	// -- Not implemented methods --

	override suspend fun runCommand(command: Bson): Document = error("not implemented")
	override suspend fun runCommand(command: Bson, readPreference: ReadPreference): Document = error("not implemented")
	override suspend fun <TResult : Any> runCommand(command: Bson, resultClass: KClass<out TResult>): TResult = error("not implemented")
	override suspend fun <TResult : Any> runCommand(command: Bson, readPreference: ReadPreference, resultClass: KClass<out TResult>): TResult = error("not implemented")
	override suspend fun runCommand(clientSession: ClientSession, command: Bson): Document = error("not implemented")
	override suspend fun runCommand(clientSession: ClientSession, command: Bson, readPreference: ReadPreference): Document = error("not implemented")
	override suspend fun <TResult : Any> runCommand(clientSession: ClientSession, command: Bson, resultClass: KClass<out TResult>): TResult = error("not implemented")
	override suspend fun <TResult : Any> runCommand(clientSession: ClientSession, command: Bson, readPreference: ReadPreference, resultClass: KClass<out TResult>): TResult = error("not implemented")

	override suspend fun drop(): Unit = error("not implemented")
	override suspend fun drop(clientSession: ClientSession): Unit = error("not implemented")

	override fun listCollectionNames(): kotlinx.coroutines.flow.Flow<String> = error("not implemented")
	override fun listCollectionNames(clientSession: ClientSession): kotlinx.coroutines.flow.Flow<String> = error("not implemented")
	override fun listCollections(): ListCollectionsFlow<Document> = error("not implemented")
	override fun <TResult : Any> listCollections(resultClass: KClass<out TResult>): ListCollectionsFlow<TResult> = error("not implemented")
	override fun listCollections(clientSession: ClientSession): ListCollectionsFlow<Document> = error("not implemented")
	override fun <TResult : Any> listCollections(clientSession: ClientSession, resultClass: KClass<out TResult>): ListCollectionsFlow<TResult> = error("not implemented")

	override suspend fun createCollection(collectionName: String): Unit = error("not implemented")
	override suspend fun createCollection(collectionName: String, options: CreateCollectionOptions): Unit = error("not implemented")
	override suspend fun createCollection(clientSession: ClientSession, collectionName: String): Unit = error("not implemented")
	override suspend fun createCollection(clientSession: ClientSession, collectionName: String, options: CreateCollectionOptions): Unit = error("not implemented")

	override suspend fun createView(viewName: String, viewOn: String, pipeline: List<Bson>): Unit = error("not implemented")
	override suspend fun createView(viewName: String, viewOn: String, pipeline: List<Bson>, createViewOptions: CreateViewOptions): Unit = error("not implemented")
	override suspend fun createView(clientSession: ClientSession, viewName: String, viewOn: String, pipeline: List<Bson>): Unit = error("not implemented")
	override suspend fun createView(clientSession: ClientSession, viewName: String, viewOn: String, pipeline: List<Bson>, createViewOptions: CreateViewOptions): Unit = error("not implemented")

	override fun watch(): ChangeStreamFlow<Document> = error("not implemented")
	override fun <TResult : Any> watch(resultClass: KClass<out TResult>): ChangeStreamFlow<TResult> = error("not implemented")
	override fun watch(pipeline: List<Bson>): ChangeStreamFlow<Document> = error("not implemented")
	override fun <TResult : Any> watch(pipeline: List<Bson>, resultClass: KClass<out TResult>): ChangeStreamFlow<TResult> = error("not implemented")
	override fun watch(clientSession: ClientSession): ChangeStreamFlow<Document> = error("not implemented")
	override fun <TResult : Any> watch(clientSession: ClientSession, resultClass: KClass<out TResult>): ChangeStreamFlow<TResult> = error("not implemented")
	override fun watch(clientSession: ClientSession, pipeline: List<Bson>): ChangeStreamFlow<Document> = error("not implemented")
	override fun <TResult : Any> watch(clientSession: ClientSession, pipeline: List<Bson>, resultClass: KClass<out TResult>): ChangeStreamFlow<TResult> = error("not implemented")

	override fun aggregate(pipeline: List<Bson>): AggregateFlow<Document> = error("not implemented")
	override fun <TResult : Any> aggregate(pipeline: List<Bson>, resultClass: KClass<out TResult>): AggregateFlow<TResult> = error("not implemented")
	override fun aggregate(clientSession: ClientSession, pipeline: List<Bson>): AggregateFlow<Document> = error("not implemented")
	override fun <TResult : Any> aggregate(clientSession: ClientSession, pipeline: List<Bson>, resultClass: KClass<out TResult>): AggregateFlow<TResult> = error("not implemented")
}
