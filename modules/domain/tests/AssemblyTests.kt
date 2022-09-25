import BankAccountCommand.*
import BankAccountEvent.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.cqrs.*
import io.fluidsonic.raptor.transactions.*
import kotlin.test.*


class AssemblyTests {

	@Test
	fun testNewAggregate() {
		val store = TestAggregateStore()

		val raptor = raptor {
			install(RaptorDomainPlugin)
			install(RaptorTransactionPlugin)

			domain.aggregates {
				store(store)

				new(::BankAccountAggregate, "bank account") {
					project(::BankAccountProjector)

					command<Create>()
					command<Delete>()
					command<Deposit>()
					command<Label>()
					command<Withdraw>()

					event<Created>("created")
					event<Deleted>("deleted")
					event<Deposited>("deposited")
					event<Labeled>("labeled")
					event<Withdrawn>("withdrawn")
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
						eventClass = BankAccountEvent::class,
						eventDefinitions = setOf(
							RaptorAggregateEventDefinition(discriminator = "created", eventClass = Created::class),
							RaptorAggregateEventDefinition(discriminator = "deleted", eventClass = Deleted::class),
							RaptorAggregateEventDefinition(discriminator = "deposited", eventClass = Deposited::class),
							RaptorAggregateEventDefinition(discriminator = "labeled", eventClass = Labeled::class),
							RaptorAggregateEventDefinition(discriminator = "withdrawn", eventClass = Withdrawn::class),
						),
						factory = RaptorAggregateFactory(::BankAccountAggregate),
						idClass = BankAccountNumber::class,
					),
				),
				store = store,
			)),
		)
	}
}
