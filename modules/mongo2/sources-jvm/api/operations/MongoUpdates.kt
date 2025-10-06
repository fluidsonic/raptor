package io.fluidsonic.raptor.mongo2

import com.mongodb.client.model.*
import com.mongodb.client.model.PushOptions as SourcePushOptions
import org.bson.conversions.*


public object MongoUpdates {

	public fun addEachToSet(fieldName: String, values: List<Any?>): Bson = Updates.addEachToSet(fieldName, values)
	public fun addToSet(fieldName: String, value: Any?): Bson = Updates.addToSet(fieldName, value)
	public fun bitwiseAnd(fieldName: String, value: Int): Bson = Updates.bitwiseAnd(fieldName, value)
	public fun bitwiseAnd(fieldName: String, value: Long): Bson = Updates.bitwiseAnd(fieldName, value)
	public fun bitwiseOr(fieldName: String, value: Int): Bson = Updates.bitwiseOr(fieldName, value)
	public fun bitwiseOr(fieldName: String, value: Long): Bson = Updates.bitwiseOr(fieldName, value)
	public fun bitwiseXor(fieldName: String, value: Int): Bson = Updates.bitwiseXor(fieldName, value)
	public fun bitwiseXor(fieldName: String, value: Long): Bson = Updates.bitwiseXor(fieldName, value)
	public fun combine(updates: List<Bson>): Bson = Updates.combine(updates)

	@SafeVarargs
	public fun combine(vararg updates: Bson?): Bson = Updates.combine(*updates)

	public fun currentDate(fieldName: String): Bson = Updates.currentDate(fieldName)
	public fun currentTimestamp(fieldName: String): Bson = Updates.currentTimestamp(fieldName)
	public fun inc(fieldName: String, number: Number): Bson = Updates.inc(fieldName, number)
	public fun max(fieldName: String, value: Any): Bson = Updates.max(fieldName, value)
	public fun min(fieldName: String, value: Any): Bson = Updates.min(fieldName, value)
	public fun mul(fieldName: String, number: Number): Bson = Updates.mul(fieldName, number)
	public fun popFirst(fieldName: String): Bson = Updates.popFirst(fieldName)
	public fun popLast(fieldName: String): Bson = Updates.popLast(fieldName)
	public fun pull(fieldName: String, value: Any?): Bson = Updates.pull(fieldName, value)
	public fun pullAll(fieldName: String, values: List<Any?>): Bson = Updates.pullAll(fieldName, values)
	public fun pullByFilter(filter: Bson): Bson = Updates.pullByFilter(filter)
	public fun push(fieldName: String, value: Any?): Bson = Updates.push(fieldName, value)
	public fun pushEach(fieldName: String, values: List<Any?>): Bson = Updates.pushEach(fieldName, values)
	public fun pushEach(fieldName: String, values: List<Any?>, options: PushOptions): Bson = Updates.pushEach(fieldName, values, options.toSource())
	public fun rename(fieldName: String, newFieldName: String): Bson = Updates.rename(fieldName, newFieldName)
	public fun set(fieldName: String, value: Any?): Bson = Updates.set(fieldName, value)
	public fun setOnInsert(fieldName: String, value: Any?): Bson = Updates.setOnInsert(fieldName, value)
	public fun setOnInsert(value: Bson): Bson = Updates.setOnInsert(value)
	public fun unset(fieldName: String): Bson = Updates.unset(fieldName)


	public data class PushOptions(
		val position: Int? = null,
		val slice: Int? = null,
		val sort: Int? = null,
		val sortDocument: Bson? = null,
	) {

		init {
			require((sort == null || sortDocument == null)) { "Cannot set both, `sort` and `sortDocument`." }
		}


		internal fun toSource(): SourcePushOptions =
			SourcePushOptions().apply {
				position?.let(::position)
				slice?.let(::slice)
				sort?.let(::sort)
				sortDocument?.let(::sortDocument)
			}
	}
}
