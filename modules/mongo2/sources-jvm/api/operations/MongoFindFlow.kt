package io.fluidsonic.raptor.mongo2

import kotlinx.coroutines.flow.*
import org.bson.conversions.*


public interface MongoFindFlow<out Value> : Flow<Value> {

	public fun filter(filter: Bson?): MongoFindFlow<Value>
	public fun limit(limit: Int): MongoFindFlow<Value>
	public fun projection(projection: Bson?): MongoFindFlow<Value>
	public fun <TransformedValue> transformValueFlow(transform: (values: Flow<Value>) -> Flow<TransformedValue>): MongoFindFlow<TransformedValue>


	public suspend fun firstOrNull(): Value? =
		(limit(1) as Flow<Value>).firstOrNull()


	public companion object
}


public inline fun <T, R> MongoFindFlow<T>.map(
	crossinline transform: suspend (value: T) -> R,
): MongoFindFlow<R> = transformValueFlow { it.map(transform) }


public inline fun <T, R : Any> MongoFindFlow<T>.mapNotNull(
	crossinline transform: suspend (value: T) -> R?,
): MongoFindFlow<R> = transformValueFlow { it.mapNotNull(transform) }
