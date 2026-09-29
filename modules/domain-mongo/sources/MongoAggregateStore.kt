package io.fluidsonic.raptor.domain.mongo

import com.mongodb.*
import com.mongodb.client.model.*
import com.mongodb.client.model.Sorts.*
import io.fluidsonic.mongo.*
import io.fluidsonic.raptor.domain.*
import io.fluidsonic.raptor.domain.mongo.RaptorAggregateEventBson.Fields
import io.fluidsonic.raptor.mongo.*
import io.fluidsonic.time.*
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


	override suspend fun lastEventTimestampOrNull(): Timestamp? =
		collection.find()
			.sort(descending(Fields.id))
			.firstOrNull()
			?.timestamp


	override fun load(after: RaptorAggregateEventId?): Flow<RaptorAggregateEvent<*, *>> =
		collection.find()
			// Any significantly large batch size is fine. We max out what MongoDB allows. 4-6x speed increase until here.
			.batchSize(1_000_000)
			.let { events ->
				when (after) {
					null -> events
					else -> events.filter(Filters.gt(Fields.id, after))
				}
			}
			.sort(ascending(Fields.id))


	override fun <Id : RaptorAggregateId> loadAggregate(
		definition: RaptorAggregateDefinition<*, out Id, *, *>,
		id: Id,
		afterVersion: Int?,
	): Flow<RaptorAggregateEvent<*, *>> {
		require(definition.idClass == id::class) {
			"`id` must be of type `${definition.idClass.qualifiedName}` for aggregate '${definition.discriminator}', but was `${id::class.qualifiedName}`: $id"
		}
		check(!definition.isIndividual) {
			"Cannot load events for individual aggregate '${definition.discriminator}' via `loadAggregate`; use its dedicated `RaptorIndividualAggregateStore` instead."
		}

		return collection.find()
			// A single aggregate's history is small; a large batch size costs nothing and avoids
			// unnecessary round-trips.
			.batchSize(1_000_000)
			.filter(Filters.and(
				listOfNotNull(
					Filters.eq(Fields.aggregateType, definition.discriminator),
					Filters.eq(Fields.aggregateId, id),
					afterVersion?.let { Filters.gt(Fields.version, it) },
				)
			))
			.sort(ascending(Fields.version))
	}


	override fun loadPage(
		limit: Int,
		changes: Map<RaptorAggregateDefinition<*, *, *, *>, Set<RaptorAggregateChangeDefinition<*, *>>?>?,
		before: RaptorAggregateEventId?,
		after: RaptorAggregateEventId?,
		descending: Boolean,
	): Flow<RaptorAggregateEvent<*, *>> {
		require(limit in 1..RaptorAggregateLoader.MAX_PAGE_SIZE) {
			"`limit` must be in 1..${RaptorAggregateLoader.MAX_PAGE_SIZE}, but was $limit."
		}
		require(before == null || after == null || before > after) {
			"`before` ($before) must be greater than `after` ($after)."
		}

		val changeFilters = changes?.entries?.map { (definition, changeDefinitions) ->
			check(!definition.isIndividual) {
				"Cannot load events for individual aggregate '${definition.discriminator}' via `loadPage`; use its dedicated `RaptorIndividualAggregateStore` instead."
			}
			require(changeDefinitions == null || changeDefinitions.all { changeDefinition -> definition.changeDefinitions.any { it == changeDefinition } }) {
				"`changes` contains a change definition that does not belong to aggregate '${definition.discriminator}'."
			}

			// "All changes" is expanded to the aggregate's registered discriminators instead of matching on
			// `aggregateType` alone: an unbounded `changeType` would keep the index from returning `_id` order.
			// An event with an unregistered discriminator could not be decoded anyway.
			Filters.and(
				Filters.eq(Fields.aggregateType, definition.discriminator),
				Filters.`in`(Fields.changeType, (changeDefinitions ?: definition.changeDefinitions).map { it.discriminator }),
			)
		}

		val filters = listOfNotNull(
			before?.let { Filters.lt(Fields.id, it) },
			after?.let { Filters.gt(Fields.id, it) },
			// `$or` of nothing is invalid in MongoDB; an empty map matches nothing.
			changeFilters?.let { if (it.isEmpty()) return emptyFlow() else Filters.or(it) },
		)

		return collection.find()
			.filter(if (filters.isEmpty()) Filters.empty() else Filters.and(filters))
			.sort(if (descending) descending(Fields.id) else ascending(Fields.id))
			.limit(limit)
	}


	override suspend fun start() {
		coroutineScope {
			launch {
				collection.createIndex(
					Indexes.ascending(Fields.aggregateType, Fields.aggregateId, Fields.version),
					IndexOptions().background(true).unique(true),
				)
			}
			launch {
				// Serves `loadPage`: each `$or` branch walks this index and MongoDB merge-sorts the branches by `_id`.
				collection.createIndex(
					Indexes.ascending(Fields.aggregateType, Fields.changeType, Fields.id),
					IndexOptions().background(true),
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
