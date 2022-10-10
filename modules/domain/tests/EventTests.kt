import BankAccountChange.*
import io.fluidsonic.raptor.cqrs.*
import io.fluidsonic.time.*
import kotlin.test.*


class EventTests {

	@Test
	fun testRequiresPositiveVersionNumber() {
		assertEquals(
			actual = assertFails {
				RaptorAggregateEvent(
					aggregateId = BankAccountNumber("1"),
					data = Created(owner = "owner"),
					id = RaptorAggregateEventId("event 1"),
					timestamp = Timestamp.fromEpochSeconds(0),
					version = 0,
				)
			}.message,
			expected = "Version number must be positive: 0"
		)
	}
}
