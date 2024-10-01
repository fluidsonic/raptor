package io.fluidsonic.raptor.domain.mongo

import com.mongodb.*
import io.fluidsonic.mongo.*
import io.fluidsonic.raptor.domain.*
import kotlin.reflect.*


private class MongoIndividualAggregateStoreFactory(
	private val client: MongoClient,
	private val database: MongoDatabase,
	private val transactionOptions: TransactionOptions,
) : RaptorIndividualAggregateStoreFactory {

	override fun create(
		name: String,
		eventType: KType, /* <RaptorAggregateEvent<Id, Change>> */
	): RaptorIndividualAggregateStore<*, *> =
		RaptorIndividualAggregateStore.mongo(
			client = client,
			collectionName = name,
			database = database,
			eventType = eventType,
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
