import BankAccountEvent.*
import io.fluidsonic.raptor.cqrs.*
import io.fluidsonic.time.*
import kotlin.test.*


class EventTests {

	@Test
	fun testRequiresPositiveVersionNumber() {
		assertEquals(
			actual = assertFails {
				RaptorEvent(
					aggregateId = BankAccountNumber("1"),
					data = Created(owner = "owner"),
					id = RaptorEventId("event 1"),
					timestamp = Timestamp.fromEpochSeconds(0),
					version = 0,
				)
			}.message,
			expected = "Version number must be positive: 0"
		)
	}
}
