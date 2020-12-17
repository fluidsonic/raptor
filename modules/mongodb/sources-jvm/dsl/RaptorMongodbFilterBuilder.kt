package io.fluidsonic.raptor

import com.mongodb.client.model.*
import org.bson.conversions.*


@RaptorDsl
public class RaptorMongodbFilterBuilder @PublishedApi internal constructor() {

	private val conditions: MutableList<Bson> = mutableListOf()


	@PublishedApi
	internal fun build() =
		when (conditions.size) {
			0 -> null
			1 -> conditions.single()
			else -> Filters.and(conditions)
		}


	@RaptorDsl
	public fun eq(fieldName: String, value: Any?) {
		conditions += Filters.eq(fieldName, value)
	}


	@RaptorDsl
	public fun id(value: Any?) {
		conditions += Filters.eq(value)
	}
}
