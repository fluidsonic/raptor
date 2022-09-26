package io.fluidsonic.raptor.domain.mongo

import io.fluidsonic.mongo.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.cqrs.*
import kotlinx.datetime.*


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorAggregatesComponent>.mongoStore(
	clock: Clock,
	collection: MongoCollection<RaptorEvent<*, *>>,
) {
	each {
		store(RaptorMongoAggregateStore(clock = clock, collection = collection))
	}
}
