package io.fluidsonic.raptor.keyvaluestore.mongo2

import io.fluidsonic.raptor.keyvaluestore.*
import io.fluidsonic.raptor.mongo2.*


public fun RaptorKeyValueStoreFactory.Companion.mongo2(database: MutableMongoDatabase): RaptorKeyValueStoreFactory =
	MongoKeyValueStoreFactory(database = database)
