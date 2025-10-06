package io.fluidsonic.raptor.mongo2

import com.mongodb.reactivestreams.client.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.*
import org.bson.conversions.*
import org.reactivestreams.*


private data class DefaultMongoFindFlow<Value>(
	val source: FindPublisher<*>,
	private val valueFlow: Flow<Value>,
) : MongoFindFlow<Value>, Flow<Value> by valueFlow {

	override fun filter(filter: Bson?) = also {
		source.filter(filter)
	}


	override fun limit(limit: Int) = also {
		source.limit(limit)
	}


	override fun projection(projection: Bson?) = also {
		source.projection(projection)
	}


	override fun <TransformedValue> transformValueFlow(
		transform: (values: Flow<Value>) -> Flow<TransformedValue>,
	): MongoFindFlow<TransformedValue> =
		DefaultMongoFindFlow(
			source = source,
			valueFlow = transform(valueFlow),
		)
}


@Suppress("FunctionName", "UNCHECKED_CAST")
internal fun <Value> DefaultMongoFindFlow(source: FindPublisher<Value>): MongoFindFlow<Value> =
	DefaultMongoFindFlow<Any?>(
		source = source,
		valueFlow = (source as Publisher<Value & Any>).asFlow(),
	) as MongoFindFlow<Value>
