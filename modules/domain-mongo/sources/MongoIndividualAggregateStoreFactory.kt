package io.fluidsonic.raptor.domain.mongo

import com.mongodb.*
import io.fluidsonic.mongo.*
import io.fluidsonic.raptor.domain.*
import io.fluidsonic.raptor.mongo.*
import kotlin.reflect.*


private class MongoIndividualAggregateStoreFactory(
	private val client: MongoClient,
	private val database: MongoDatabase,
	private val transactionOptions: TransactionOptions,
) : RaptorIndividualAggregateStoreFactory {

	override fun <Id : RaptorAggregateId, Change : RaptorAggregateChange<Id>> create(
		name: String,
		eventType: KType, /* <RaptorAggregateEvent<Id, Change>> */
	): RaptorIndividualAggregateStore<Id, Change> =
		RaptorIndividualAggregateStore.mongo(
			client = client,
			collection = database.getCollectionOfGeneric(name, eventType),
			transactionOptions = transactionOptions,
		)
}


public fun RaptorIndividualAggregateStoreFactory.Companion.mongo(
	client: MongoClient,
	databaseName: String,
	transactionOptions: TransactionOptions = TransactionOptions.builder().build(),
): RaptorIndividualAggregateStoreFactory =
	MongoIndividualAggregateStoreFactory(
		client = client,
		database = client.getDatabase(databaseName),
		transactionOptions = transactionOptions,
	)
