package io.fluidsonic.raptor.domain.mongo

import io.fluidsonic.mongo.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.domain.*
import kotlinx.datetime.*


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorAggregatesComponent>.mongoEventFactory(clock: Clock) {
	each {
		eventFactory(RaptorMongoAggregateEventFactory(clock = clock))
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorAggregatesComponent>.mongoStore(collection: MongoCollection<RaptorAggregateEvent<*, *>>) {
	each {
		store(RaptorMongoAggregateStore(collection = collection))
	}
}
