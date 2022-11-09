package io.fluidsonic.raptor.domain.mongo

import com.mongodb.*
import io.fluidsonic.mongo.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.domain.*


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorAggregatesComponent>.mongoStore(
	client: MongoClient,
	databaseName: String,
	collectionName: String,
	transactionOptions: TransactionOptions = TransactionOptions.builder().build(),
) {
	each {
		store(RaptorAggregateStore.mongo(
			client = client,
			databaseName = databaseName,
			collectionName = collectionName,
			transactionOptions = transactionOptions,
		))
	}
}
