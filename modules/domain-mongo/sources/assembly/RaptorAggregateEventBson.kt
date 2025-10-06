package io.fluidsonic.raptor.domain.mongo

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import io.fluidsonic.raptor.domain.*
import io.fluidsonic.time.*
import kotlin.reflect.*


internal object RaptorAggregateEventBson {

	fun bson(definitions: Set<RaptorAggregateDefinition<*, *, *, *>>): RaptorBsonDefinition {
		// FIXME Move to new RaptorAggregatesDefinition?
		val definitionsByDiscriminator: MutableMap<String, RaptorAggregateDefinition<*, *, *, *>> =
			definitions.associateByTo(hashMapOf()) { it.discriminator }

		val definitionsByIdClass: MutableMap<KClass<out RaptorAggregateId>, RaptorAggregateDefinition<*, *, *, *>> =
			definitions.associateByTo(hashMapOf()) { it.idClass }

		return raptor.bson.definition<RaptorAggregateEvent<*, *>> {
			decode {
				var aggregateId: RaptorAggregateId? = null
				var change: RaptorAggregateChange<*>? = null
				var id: RaptorAggregateEventId? = null
				var lastVersionInBatch: Int? = null
				var timestamp: Timestamp? = null
				var version: Int? = null

				var definition: RaptorAggregateDefinition<*, RaptorAggregateId, *, RaptorAggregateChange<RaptorAggregateId>>? = null
				var changeDefinition: RaptorAggregateChangeDefinition<RaptorAggregateId, out RaptorAggregateChange<RaptorAggregateId>>? = null

				reader.documentByField { field ->
					when (field) {
						Fields.aggregateType -> string().let { discriminator ->
							@Suppress("UNCHECKED_CAST")
							definition = definitionsByDiscriminator[discriminator]
								as RaptorAggregateDefinition<*, RaptorAggregateId, *, RaptorAggregateChange<RaptorAggregateId>>?
								?: error("Cannot decode event for undefined aggregate discriminator '$discriminator'.")
						}

						Fields.aggregateId -> {
							@Suppress("NAME_SHADOWING")
							val definition = checkNotNull(definition) {
								"Invalid field order when decoding ${RaptorAggregateEvent::class}. '${Fields.aggregateType}' must precede '${Fields.aggregateId}'."
							}

							aggregateId = value(definition.idType)
						}

						Fields.changeType -> string().let { discriminator ->
							@Suppress("NAME_SHADOWING")
							val definition = checkNotNull(definition) {
								"Invalid field order when decoding ${RaptorAggregateEvent::class}. '${Fields.aggregateType}' must precede '${Fields.changeType}'."
							}

							changeDefinition = definition.changeDefinition(discriminator)
								?: error("Cannot decode undefined event discriminator '$discriminator' for aggregate '${definition.discriminator}'.")
						}

						Fields.change -> {
							@Suppress("NAME_SHADOWING")
							val changeDefinition = checkNotNull(changeDefinition) {
								"Invalid field order when decoding ${RaptorAggregateEvent::class}. '${Fields.changeType}' must precede '${Fields.change}'."
							}

							change = value(changeDefinition.type)
						}

						Fields.id -> id = value()
						Fields.lastVersionInBatch -> lastVersionInBatch = int()
						Fields.timestamp -> timestamp = value()
						Fields.version -> version = int()
						else -> skipValue()
					}
				}

				RaptorAggregateEvent(
					aggregateId = aggregateId ?: missingFieldValue(Fields.aggregateId),
					change = change ?: missingFieldValue(Fields.change),
					id = id ?: missingFieldValue(Fields.id),
					lastVersionInBatch = lastVersionInBatch ?: missingFieldValue(Fields.lastVersionInBatch),
					timestamp = timestamp ?: missingFieldValue(Fields.timestamp),
					version = version ?: missingFieldValue(Fields.version),
				)
			}

			encode { value ->
				@Suppress("UNCHECKED_CAST")
				val definition = definitionsByIdClass[value.aggregateId::class]
					as RaptorAggregateDefinition<*, RaptorAggregateId, *, RaptorAggregateChange<RaptorAggregateId>>?
					?: error("Cannot encode event for undefined aggregate '${value.aggregateId.debug}':\n$value")

				val changeDefinition = definition.changeDefinition(value.change::class)
					?: error("Cannot encode undefined change for aggregate '${value.aggregateId.debug}':\n$value")

				writer.document {
					value(Fields.aggregateType, definition.discriminator)
					value(Fields.aggregateId, value.aggregateId)
					value(Fields.changeType, changeDefinition.discriminator)
					value(Fields.change, value.change)
					value(Fields.id, value.id)
					value(Fields.lastVersionInBatch, value.lastVersionInBatch)
					value(Fields.timestamp, value.timestamp)
					value(Fields.version, value.version)
				}
			}
		}
	}


	fun idBson() = raptor.bson.definition<RaptorAggregateEventId> {
		decode(::RaptorAggregateEventId)
		encode(RaptorAggregateEventId::toLong)
	}


	object Fields {

		const val aggregateId = "aggregateId"
		const val aggregateType = "aggregateType"
		const val change = "change"
		const val changeType = "changeType"
		const val id = "_id"
		const val lastVersionInBatch = "lastVersionInBatch"
		const val timestamp = "timestamp"
		const val version = "version"
	}
}
