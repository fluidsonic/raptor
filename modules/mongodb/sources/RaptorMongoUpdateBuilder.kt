package io.fluidsonic.raptor

import com.mongodb.client.model.*
import io.fluidsonic.stdlib.*
import org.bson.conversions.*


// FIXME add DSL for filter
// FIXME add Updates.* methods
@RaptorDsl
class RaptorMongoUpdateBuilder @PublishedApi internal constructor() {

	private var filter: Bson? = null
	private val updates: MutableList<Bson> = mutableListOf()


	@PublishedApi
	internal fun build() =
		RaptorMongoUpdate(
			filter = filter ?: error("A filter must be defined: filter(…)"),
			changes = updates
		)


	@RaptorDsl
	fun filter(filter: Bson) {
		check(this.filter == null) { "Cannot define multiple filters." }

		this.filter = filter
	}


	@RaptorDsl
	fun setOrUnsetIfNull(fieldName: String, value: Any?) {
		updates +=
			if (value != null) Updates.set(fieldName, value)
			else Updates.unset(fieldName)
	}


	@RaptorDsl
	fun setOrUnsetIfNull(fieldName: String, maybeValue: Maybe<*>) {
		if (maybeValue.hasValue())
			setOrUnsetIfNull(fieldName = fieldName, value = maybeValue.get())
	}
}
