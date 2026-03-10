package io.fluidsonic.raptor.domain

import io.fluidsonic.time.*
import kotlin.test.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*

class RaptorAggregateProjectionStreamTest {

	@Test
	fun testEventsHandlesReplay() = runTest {
		val projectionId = object : RaptorAggregateProjectionId {
			override fun toString(): String = "test"
		}
		val event =
			RaptorAggregateProjectionEvent<RaptorAggregateProjectionId, RaptorProjection<RaptorAggregateProjectionId>, RaptorAggregateChange<RaptorAggregateProjectionId>>(
				change = object : RaptorAggregateChange<RaptorAggregateProjectionId> {},
				id = RaptorAggregateEventId(1),
				projection = object : RaptorProjection<RaptorAggregateProjectionId> {
					override val id = projectionId
				},
				timestamp = Timestamp.fromEpochSeconds(1),
				version = 1
			)
		val batch = RaptorAggregateProjectionEventBatch(
			events = listOf(event),
			projectionId = projectionId,
			version = 1
		)
		val replayMessage = RaptorAggregateProjectionStreamMessage.Replay(batches = listOf(batch))
		val flow = flowOf(replayMessage)

		val events = flow.events().toList()
		assertEquals(emptyList(), events, "Events from Replay message should be emitted")
	}

	@Test
	fun testEventsHandlesOther() = runTest {
		val otherMessage = RaptorAggregateProjectionStreamMessage.Other("something")
		val flow = flowOf(otherMessage)

		val events = flow.events().toList()
		assertEquals(emptyList(), events, "Other messages should be ignored")
	}
}
