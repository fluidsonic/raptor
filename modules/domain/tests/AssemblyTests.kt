import BankAccountChange.*
import BankAccountCommand.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.cqrs.*
import io.fluidsonic.raptor.transactions.*
import kotlin.test.*
import kotlinx.datetime.*


class AssemblyTests {

	@Test
	fun testNewAggregate() {
		val eventFactory = TestAggregateEventFactory(clock = Clock.System)
		val store = TestAggregateStore()

		val raptor = raptor {
			install(RaptorDomainPlugin)
			install(RaptorLifecyclePlugin)
			install(RaptorTransactionPlugin)

			domain.aggregates {
				eventFactory(eventFactory)
				store(store)

				new(::BankAccountAggregate, "bank account") {
					project(::BankAccountProjector)

					command<Create>()
					command<Delete>()
					command<Deposit>()
					command<Label>()
					command<Withdraw>()

					change<Created>("created")
					change<Deleted>("deleted")
					change<Deposited>("deposited")
					change<Labeled>("labeled")
					change<Withdrawn>("withdrawn")
				}
			}
		}

		assertEquals(
			actual = raptor.context.domain,
			expected = RaptorDomain(aggregates = RaptorDomain.Aggregates(
				definitions = setOf(
					RaptorAggregateDefinition(
						aggregateClass = BankAccountAggregate::class,
						commandClass = BankAccountCommand::class,
						commandDefinitions = setOf(
							RaptorAggregateCommandDefinition(commandClass = Create::class),
							RaptorAggregateCommandDefinition(commandClass = Delete::class),
							RaptorAggregateCommandDefinition(commandClass = Deposit::class),
							RaptorAggregateCommandDefinition(commandClass = Label::class),
							RaptorAggregateCommandDefinition(commandClass = Withdraw::class),
						),
						discriminator = "bank account",
						eventClass = BankAccountChange::class,
						eventDefinitions = setOf(
							RaptorAggregateChangeDefinition(discriminator = "created", eventClass = Created::class),
							RaptorAggregateChangeDefinition(discriminator = "deleted", eventClass = Deleted::class),
							RaptorAggregateChangeDefinition(discriminator = "deposited", eventClass = Deposited::class),
							RaptorAggregateChangeDefinition(discriminator = "labeled", eventClass = Labeled::class),
							RaptorAggregateChangeDefinition(discriminator = "withdrawn", eventClass = Withdrawn::class),
						),
						factory = RaptorAggregateFactory(::BankAccountAggregate),
						idClass = BankAccountNumber::class,
						projectionDefinition = RaptorAggregateProjectionDefinition(
							factory = ::BankAccountProjector,
							idClass = BankAccountNumber::class,
							projectionClass = BankAccount::class
						),
					),
				),
				eventFactory = eventFactory,
				store = store,
			)),
		)
	}
}
