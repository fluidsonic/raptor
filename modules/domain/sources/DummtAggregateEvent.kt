package io.fluidsonic.raptor.domain

import io.fluidsonic.time.*


internal fun dummyAggregateEvent(): RaptorAggregateEvent<*, *> =
	RaptorAggregateEvent(
		aggregateId = DummyAggregateId,
		change = DummyAggregateChange,
		id = RaptorAggregateEventId("dummy"),
		isReplay = false,
		timestamp = Timestamp.fromEpochSeconds(0),
		version = 1,
	)


internal fun RaptorAggregateEvent<*, *>.isDummy() =
	aggregateId === DummyAggregateId


private object DummyAggregateChange : RaptorAggregateChange<DummyAggregateId>

private object DummyAggregateId : RaptorAggregateId {

	override fun toString() = "dummy"
}
