package io.fluidsonic.raptor.domain.mongo

import com.mongodb.*
import com.mongodb.client.model.*
import com.mongodb.client.model.Sorts.*
import io.fluidsonic.mongo.*
import io.fluidsonic.raptor.domain.*
import io.fluidsonic.raptor.domain.mongo.RaptorAggregateEventBson.Fields
import io.fluidsonic.raptor.mongo.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


// TODO Support horizontal scaling.
private class MongoAggregateStore(
	private val client: MongoClient,
	private val collection: MongoCollection<RaptorAggregateEvent<*, *>>,
	private val transactionOptions: TransactionOptions,
) : RaptorAggregateStore {

	override suspend fun add(events: List<RaptorAggregateEvent<*, *>>) {
		// TODO Batch in case the number of events is large.
		try {
			client.transaction(transactionOptions) { session ->
				collection.insertMany(session, events, InsertManyOptions().ordered(false))
			}
		}
		catch (e: MongoBulkWriteException) {
			// FIXME Check for duplicate key errors.
			// throw RaptorAggregateVersionConflict(e)

			// FIXME We actually cannot recover from this without stopping Raptor & starting a new one.
			//       We only support a single instance for now.
			throw e
		}
	}


	override fun load(): Flow<RaptorAggregateEvent<*, *>> =
		collection.find().sort(ascending(Fields.id))


	override suspend fun start() {
		coroutineScope {
			launch {
				collection.createIndex(
					Indexes.ascending(Fields.aggregateType, Fields.aggregateId, Fields.version),
					IndexOptions().unique(true),
				)
			}
		}
	}
}


public fun RaptorAggregateStore.Companion.mongo(
	client: MongoClient,
	databaseName: String,
	collectionName: String,
	transactionOptions: TransactionOptions = TransactionOptions.builder().build(),
): RaptorAggregateStore =
	MongoAggregateStore(
		client = client,
		collection = client.getDatabase(databaseName).getCollectionOf(collectionName),
		transactionOptions = transactionOptions,
	)
