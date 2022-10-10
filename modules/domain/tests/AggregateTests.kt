import BankAccountChange.*
import BankAccountCommand.*
import kotlin.test.*


class AggregateTests {

	@Test
	fun testBasics() {
		val account = BankAccountAggregate(BankAccountNumber("1"))
		account.handle(Created(owner = "owner"))

		assertEquals(
			actual = account.execute(Deposit(amount = 100)),
			expected = listOf(Deposited(amount = 100)),
		)
		account.handle(Deposited(amount = 100))

		assertEquals(
			actual = account.execute(Label(label = "test")),
			expected = listOf(Labeled(label = "test")),
		)
		account.handle(Labeled(label = "test"))

		assertEquals(
			actual = assertFails {
				account.execute(Delete)
			}.message,
			expected = "Cannot delete an account that still has funds.",
		)

		assertEquals(
			actual = account.execute(Withdraw(amount = 100)),
			expected = listOf(Withdrawn(amount = 100)),
		)
		account.handle(Withdrawn(amount = 100))

		assertEquals(
			actual = account.execute(Delete),
			expected = listOf(Deleted),
		)
		account.handle(Deleted)
	}
}
