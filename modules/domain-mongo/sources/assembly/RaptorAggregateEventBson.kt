package io.fluidsonic.raptor.domain.mongo

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import io.fluidsonic.raptor.cqrs.*
import io.fluidsonic.time.*
import kotlin.reflect.*


internal class RaptorAggregateEventBson(
	definitions: Set<RaptorAggregateDefinition<*, *, *, *>>,
) : RaptorPlugin {

	// FIXME Move to new RaptorAggregatesDefinition?
	private val definitionsByDiscriminator: MutableMap<String, RaptorAggregateDefinition<*, *, *, *>> =
		definitions.associateByTo(hashMapOf()) { it.discriminator }

	private val definitionsByIdClass: MutableMap<KClass<out RaptorAggregateId>, RaptorAggregateDefinition<*, *, *, *>> =
		definitions.associateByTo(hashMapOf()) { it.idClass }


	override fun RaptorFeatureScope.install() {
		bson.definition<RaptorEvent<*, *>> {
			decode {
				var aggregateId: RaptorAggregateId? = null
				var change: RaptorAggregateEvent<*>? = null
				var id: RaptorEventId? = null
				var timestamp: Timestamp? = null
				var version: Int? = null

				var definition: RaptorAggregateDefinition<*, RaptorAggregateId, *, RaptorAggregateEvent<RaptorAggregateId>>? = null
				var changeDefinition: RaptorAggregateEventDefinition<RaptorAggregateId, out RaptorAggregateEvent<RaptorAggregateId>>? = null

				reader.documentByField { field ->
					when (field) {
						Fields.aggregateType -> string().let { discriminator ->
							@Suppress("UNCHECKED_CAST")
							definition = definitionsByDiscriminator[discriminator]
								as RaptorAggregateDefinition<*, RaptorAggregateId, *, RaptorAggregateEvent<RaptorAggregateId>>?
								?: error("Cannot decode event for undefined aggregate discriminator '$discriminator'.")
						}

						Fields.aggregateId -> {
							@Suppress("NAME_SHADOWING")
							val definition = checkNotNull(definition) {
								"Invalid field order when decoding ${RaptorEvent::class}. '${Fields.aggregateType}' must precede '${Fields.aggregateId}'."
							}

							aggregateId = value(definition.idType)
						}

						Fields.changeType -> string().let { discriminator ->
							@Suppress("NAME_SHADOWING")
							val definition = checkNotNull(definition) {
								"Invalid field order when decoding ${RaptorEvent::class}. '${Fields.aggregateType}' must precede '${Fields.changeType}'."
							}

							changeDefinition = definition.changeDefinition(discriminator)
								?: error("Cannot decode undefined event discriminator '$discriminator' for aggregate '${definition.discriminator}'.")
						}

						Fields.change -> {
							@Suppress("NAME_SHADOWING")
							val changeDefinition = checkNotNull(changeDefinition) {
								"Invalid field order when decoding ${RaptorEvent::class}. '${Fields.changeType}' must precede '${Fields.change}'."
							}

							change = value(changeDefinition.type)
						}

						Fields.id -> id = value()
						Fields.timestamp -> timestamp = value()
						Fields.version -> version = value()
						else -> skipValue()
					}
				}

				RaptorEvent(
					aggregateId = aggregateId ?: missingFieldValue(Fields.aggregateId),
					data = change ?: missingFieldValue(Fields.change),
					id = id ?: missingFieldValue(Fields.id),
					timestamp = timestamp ?: missingFieldValue(Fields.timestamp),
					version = version ?: missingFieldValue(Fields.version),
				)
			}

			encode { value ->
				@Suppress("UNCHECKED_CAST")
				val definition = definitionsByIdClass[value.aggregateId::class]
					as RaptorAggregateDefinition<*, RaptorAggregateId, *, RaptorAggregateEvent<RaptorAggregateId>>?
					?: error("Cannot encode event for undefined aggregate '${value.aggregateId.debug}':\n$value")

				val changeDefinition = definition.changeDefinition(value.data::class)
					?: error("Cannot encode undefined change for aggregate '${value.aggregateId.debug}':\n$value")

				writer.document {
					value(Fields.aggregateType, definition.discriminator)
					value(Fields.aggregateId, value.aggregateId)
					value(Fields.changeType, changeDefinition.discriminator)
					value(Fields.change, value.data)
					value(Fields.id, value.id)
					value(Fields.timestamp, value.timestamp)
					value(Fields.version, value.version)
				}
			}
		}
	}


	object Fields {

		const val aggregateId = "aggregateId"
		const val aggregateType = "aggregateType"
		const val change = "change"
		const val changeType = "changeType"
		const val id = "_id"
		const val timestamp = "timestamp"
		const val version = "version"
	}
}
