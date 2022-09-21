package io.fluidsonic.raptor.mongo

import com.mongodb.client.model.*
import io.fluidsonic.raptor.*
import org.bson.conversions.*


@RaptorDsl
public class RaptorMongodbFilterBuilder @PublishedApi internal constructor() {

	private val conditions: MutableList<Bson> = mutableListOf()


	@PublishedApi
	internal fun build(): Bson? =
		when (conditions.size) {
			0 -> null
			1 -> conditions.single()
			else -> Filters.and(conditions)
		}


	@RaptorDsl
	public fun condition(condition: Bson) {
		conditions += condition
	}


	@RaptorDsl
	public fun eq(fieldName: String, value: Any?) {
		conditions += Filters.eq(fieldName, value)
	}


	@RaptorDsl
	public fun gt(fieldName: String, value: Any) {
		conditions += Filters.gt(fieldName, value)
	}


	@RaptorDsl
	public fun gte(fieldName: String, value: Any) {
		conditions += Filters.gte(fieldName, value)
	}


	@RaptorDsl
	public fun id(value: Any?) {
		conditions += Filters.eq(value)
	}


	@RaptorDsl
	public fun lt(fieldName: String, value: Any) {
		conditions += Filters.lt(fieldName, value)
	}


	@RaptorDsl
	public fun lte(fieldName: String, value: Any) {
		conditions += Filters.lte(fieldName, value)
	}


	@RaptorDsl
	public fun ne(fieldName: String, value: Any?) {
		conditions += Filters.ne(fieldName, value)
	}


	@RaptorDsl
	public inline fun not(conditions: RaptorMongodbFilterBuilder.() -> Unit) {
		condition(Filters.not(RaptorMongodbFilterBuilder().apply(conditions).build() ?: return))
	}
}
