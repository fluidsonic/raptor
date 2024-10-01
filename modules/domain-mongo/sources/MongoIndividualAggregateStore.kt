package io.fluidsonic.raptor.domain.mongo

import com.mongodb.*
import com.mongodb.client.model.*
import io.fluidsonic.mongo.*
import io.fluidsonic.raptor.domain.*
import io.fluidsonic.raptor.mongo.*
import kotlin.reflect.*
import kotlinx.coroutines.flow.*
import org.bson.*


private class MongoIndividualAggregateStore<Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>>(
	private val client: MongoClient,
	collectionName: String,
	database: MongoDatabase,
	eventType: KType,
	private val transactionOptions: TransactionOptions,
) : RaptorIndividualAggregateStore<Id, Change> {

	init {
		require(eventType.classifier == RaptorAggregateEvent::class) { "`eventType` must have classifier `${RaptorAggregateEvent::class.qualifiedName}`." }
	}


	private val cache: MutableMap<Id, List<RaptorAggregateEvent<Id, Change>>> = hashMapOf()
	private var cachedLastEventId: RaptorAggregateEventId? = null
	private var cachedLastEventIdLoaded = false
	private val collection: MongoCollection<RaptorAggregateEvent<Id, Change>> = database.getCollectionOfGeneric(collectionName, eventType)
	private var isIndexed = false


	private fun appendToCache(id: Id, events: List<RaptorAggregateEvent<Id, Change>>) {
		cache.compute(id) { _, previousEvents ->
			previousEvents.orEmpty() + events
		}

		cachedLastEventId = events.last().id
	}


	override suspend fun lastEventId(): RaptorAggregateEventId? {
		if (cachedLastEventIdLoaded)
			return cachedLastEventId

		val id = collection.find(Document::class)
			.projection(Projections.include("_id"))
			.sort(Sorts.descending("_id"))
			.limit(1)
			.firstOrNull()
			?.getLong("_id")
			?.let(::RaptorAggregateEventId)

		cachedLastEventId = id
		cachedLastEventIdLoaded = true

		return id
	}


	private suspend fun indexIfNeeded() {
		if (isIndexed) return

		collection.createIndex(Indexes.hashed("aggregateId"), IndexOptions().background(true))

		isIndexed = true
	}


	override suspend fun load(id: Id): List<RaptorAggregateEvent<Id, Change>> {
		indexIfNeeded()

		return cache.getOrPut(id) {
			collection.find(Filters.eq("aggregateId", id)).sort(Sorts.ascending("_id")).toList()
		}
	}


	override suspend fun reload(): List<RaptorAggregateEvent<Id, Change>> {
		val events = collection.find().sort(Sorts.ascending("_id")).toList()

		cache.clear()
		cache.putAll(events.groupByTo(hashMapOf()) { it.aggregateId })

		cachedLastEventId = events.lastOrNull()?.id
		cachedLastEventIdLoaded = true

		indexIfNeeded()

		return events
	}


	private fun resetCache() {
		cache.clear()
		cachedLastEventId = null
	}


	override suspend fun save(id: Id, events: List<RaptorAggregateEvent<Id, Change>>) {
		if (events.isEmpty())
			return

		require(events.all { it.aggregateId == id }) { "All events must have aggregate id '$id': $events" }

		try {
			client.transaction(transactionOptions) { session ->
				collection.insertMany(
					clientSession = session,
					documents = events,
					options = InsertManyOptions().ordered(false),
				)
			}
		}
		catch (e: Throwable) {
			// We can't be sure anymore what was saved. Let's wipe the cache.
			resetCache()

			throw e
		}

		appendToCache(id, events)
	}
}


public fun RaptorIndividualAggregateStore.Companion.mongo(
	client: MongoClient,
	collectionName: String,
	database: MongoDatabase,
	eventType: KType,
	transactionOptions: TransactionOptions,
): RaptorIndividualAggregateStore<*, *> =
	MongoIndividualAggregateStore<RaptorAggregateId, RaptorAggregateChange<RaptorAggregateId>>(
		client = client,
		collectionName = collectionName,
		database = database,
		eventType = eventType,
		transactionOptions = transactionOptions,
	)


@JvmName("mongoGeneric")
@Suppress("UNCHECKED_CAST")
public inline fun <reified Id : RaptorAggregateId, reified Change : RaptorAggregateChange<Id>>
	RaptorIndividualAggregateStore.Companion.mongo(
	client: MongoClient,
	database: MongoDatabase,
	collectionName: String,
	transactionOptions: TransactionOptions,
): RaptorIndividualAggregateStore<Id, Change> =
	mongo(
		client = client,
		collectionName = collectionName,
		database = database,
		eventType = typeOf<RaptorAggregateEvent<Id, Change>>(),
		transactionOptions = transactionOptions,
	) as RaptorIndividualAggregateStore<Id, Change>
