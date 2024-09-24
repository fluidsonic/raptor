package io.fluidsonic.raptor.domain.mongo

import com.mongodb.*
import com.mongodb.client.model.*
import io.fluidsonic.mongo.*
import io.fluidsonic.raptor.domain.*
import io.fluidsonic.raptor.mongo.*
import io.fluidsonic.time.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.*
import org.bson.*


private class MongoIndividualAggregateStore<Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>>(
	private val client: MongoClient,
	private val collection: MongoCollection<RaptorAggregateEvent<Id, Change>>,
	private val transactionOptions: TransactionOptions,
) : RaptorIndividualAggregateStore<Id, Change> {

	private val cache: MutableMap<Id, List<RaptorAggregateEvent<Id, Change>>> = hashMapOf()
	private var cachedLastEventId: Long? = null
	private val mutex = Mutex()
	private var isIndexed = false
	private var isPreloaded = false


	private fun appendToCache(id: Id, events: List<RaptorAggregateEvent<Id, Change>>) {
		cache.compute(id) { _, previousEvents ->
			previousEvents.orEmpty() + events
		}

		cachedLastEventId = events.last().id.toLong()
	}


	private suspend fun lastEventId(): Long {
		cachedLastEventId?.let { return it }

		return collection.find(Document::class)
			.projection(Projections.include("_id"))
			.sort(Sorts.descending("_id"))
			.limit(1)
			.firstOrNull()
			?.getLong("_id")
			.let { it ?: 0 }
			.also { cachedLastEventId = it }
	}


	private suspend fun indexIfNeeded() {
		if (isIndexed) return

		collection.createIndex(Indexes.hashed("aggregateId"), IndexOptions().background(true))

		isIndexed = true
	}


	override suspend fun load(id: Id): List<RaptorAggregateEvent<Id, Change>> =
		mutex.withLock {
			indexIfNeeded()
			loadLocked(id)
		}


	private suspend fun loadLocked(id: Id): List<RaptorAggregateEvent<Id, Change>> =
		cache.getOrPut(id) {
			collection.find(Filters.eq("aggregateId", id)).sort(Sorts.ascending("_id")).toList()
		}


	override suspend fun preload() {
		mutex.withLock {
			if (isPreloaded) return

			val events = collection.find().sort(Sorts.ascending("_id")).toList()

			cache.clear()
			cache.putAll(events.groupByTo(hashMapOf()) { it.aggregateId })

			cachedLastEventId = events.lastOrNull()?.id?.toLong() ?: 0

			indexIfNeeded()

			isPreloaded = true
		}
	}


	private fun resetCache() {
		cache.clear()
		cachedLastEventId = null
	}


	override suspend fun save(id: Id, expectedVersion: Int, changes: List<Change>, timestamp: Timestamp) {
		mutex.withLock {
			val lastVersion = loadLocked(id).lastOrNull()?.version ?: 0
			if (lastVersion != expectedVersion)
				throw RaptorAggregateVersionConflict(
					"Expected aggregate ${id.debug} at version $expectedVersion but encountered version $lastVersion.",
				)

			if (changes.isEmpty())
				return

			val lastEventId = lastEventId()
			val events = changes.mapIndexed { index, change ->
				RaptorAggregateEvent(
					aggregateId = id,
					change = change,
					id = RaptorAggregateEventId(lastEventId + 1 + index),
					timestamp = timestamp,
					version = lastVersion + 1 + index,
				)
			}

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
}


public fun <Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>>
	RaptorIndividualAggregateStore.Companion.mongo(
	client: MongoClient,
	collection: MongoCollection<RaptorAggregateEvent<Id, Change>>,
	transactionOptions: TransactionOptions,
): RaptorIndividualAggregateStore<Id, Change> =
	MongoIndividualAggregateStore(
		client = client,
		collection = collection,
		transactionOptions = transactionOptions,
	)
