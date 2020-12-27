package io.fluidsonic.raptor

import com.mongodb.client.model.*
import io.fluidsonic.stdlib.*
import org.bson.conversions.*


@RaptorDsl
public class RaptorMongodbUpdateBuilder @PublishedApi internal constructor(
	private val isUpsert: Boolean = false,
) {

	private val changes: MutableList<Bson> = mutableListOf()

	@PublishedApi
	internal val filterBuilder = RaptorMongodbFilterBuilder()


	@RaptorDsl
	public fun addEachToSet(fieldName: String, value: Collection<*>) {
		changes += Updates.addEachToSet(fieldName, value as? List<*> ?: value.toList())
	}


	@RaptorDsl
	public fun addToSet(fieldName: String, value: Any?) {
		changes += Updates.addToSet(fieldName, value)
	}


	@PublishedApi
	internal fun build() =
		RaptorMongoUpdate(
			filter = filterBuilder.build(),
			changes = changes.ifEmpty { null }?.let(Updates::combine),
			isUpsert = isUpsert
		)


	@RaptorDsl
	public inline fun filter(configure: RaptorMongodbFilterBuilder.() -> Unit) {
		filterBuilder.configure()
	}


	@RaptorDsl
	public fun hasChanges(): Boolean =
		changes.isNotEmpty()


	@RaptorDsl
	public fun pullAll(fieldName: String, value: Collection<*>) {
		changes += Updates.pullAll(fieldName, value as? List<*> ?: value.toList())
	}


	@RaptorDsl
	public fun set(fieldName: String, value: Any?) {
		changes += Updates.set(fieldName, value)
	}


	@RaptorDsl
	public fun set(fieldName: String, maybeValue: Maybe<*>) {
		if (maybeValue.hasValue())
			set(fieldName = fieldName, value = maybeValue.get())
	}


	@RaptorDsl
	public fun setOnInsert(fieldName: String, value: Any?) {
		changes += Updates.setOnInsert(fieldName, value)
	}


	@RaptorDsl
	public fun setOnInsert(fieldName: String, maybeValue: Maybe<*>) {
		if (maybeValue.hasValue())
			setOnInsert(fieldName = fieldName, value = maybeValue.get())
	}


	@RaptorDsl
	public fun setOrUnsetIfNull(fieldName: String, value: Any?) {
		changes +=
			if (value != null) Updates.set(fieldName, value)
			else Updates.unset(fieldName)
	}


	@RaptorDsl
	public fun setOrUnsetIfNull(fieldName: String, maybeValue: Maybe<*>) {
		if (maybeValue.hasValue())
			setOrUnsetIfNull(fieldName = fieldName, value = maybeValue.get())
	}


	@RaptorDsl
	public fun unset(fieldName: String) {
		changes += Updates.unset(fieldName)
	}
}
