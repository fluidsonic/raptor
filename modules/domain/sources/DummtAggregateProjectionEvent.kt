package io.fluidsonic.raptor.domain

import io.fluidsonic.time.*


internal fun dummyAggregateProjectionEvent(): RaptorAggregateProjectionEvent<*, *, *> =
	RaptorAggregateProjectionEvent(
		change = DummyAggregateProjectionChange,
		id = RaptorAggregateEventId("dummy"),
		isReplay = false,
		projection = DummyProjection,
		timestamp = Timestamp.fromEpochSeconds(0),
		version = 1,
	)


internal fun RaptorAggregateProjectionEvent<*, *, *>.isDummy() =
	projection === DummyProjection


private object DummyAggregateProjectionChange : RaptorAggregateChange<DummyAggregateProjectionId>

private object DummyAggregateProjectionId : RaptorAggregateProjectionId {

	override fun toString() = "dummy"
}


private object DummyProjection : RaptorProjection<DummyAggregateProjectionId> {

	override val id: DummyAggregateProjectionId
		get() = DummyAggregateProjectionId
}
