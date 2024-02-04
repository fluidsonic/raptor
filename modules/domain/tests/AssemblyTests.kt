import BankAccountChange.*
import BankAccountCommand.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.domain.*
import io.fluidsonic.raptor.lifecycle.*
import kotlin.test.*


class AssemblyTests {

	@Test
	fun testNewAggregate() {
		val store = TestAggregateStore()

		val raptor = raptor {
			install(RaptorDIPlugin)
			install(RaptorDomainPlugin)
			install(RaptorLifecyclePlugin)

			domain.aggregates {
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

		val configuration = raptor.context.plugins.domain
		assertEquals(
			actual = configuration.aggregateDefinitions, expected = RaptorAggregateDefinitions(
				setOf(
					RaptorAggregateDefinition(
						aggregateClass = BankAccountAggregate::class,
						changeClass = BankAccountChange::class,
						changeDefinitions = setOf(
							RaptorAggregateChangeDefinition(discriminator = "created", changeClass = Created::class),
							RaptorAggregateChangeDefinition(discriminator = "deleted", changeClass = Deleted::class),
							RaptorAggregateChangeDefinition(discriminator = "deposited", changeClass = Deposited::class),
							RaptorAggregateChangeDefinition(discriminator = "labeled", changeClass = Labeled::class),
							RaptorAggregateChangeDefinition(discriminator = "withdrawn", changeClass = Withdrawn::class),
						),
						commandClass = BankAccountCommand::class,
						commandDefinitions = setOf(
							RaptorAggregateCommandDefinition(commandClass = Create::class),
							RaptorAggregateCommandDefinition(commandClass = Delete::class),
							RaptorAggregateCommandDefinition(commandClass = Deposit::class),
							RaptorAggregateCommandDefinition(commandClass = Label::class),
							RaptorAggregateCommandDefinition(commandClass = Withdraw::class),
						),
						discriminator = "bank account",
						factory = RaptorAggregateFactory(::BankAccountAggregate),
						idClass = BankAccountNumber::class,
						isIndividual = false,
						projectionDefinition = RaptorAggregateProjectionDefinition(
							factory = ::BankAccountProjector,
							idClass = BankAccountNumber::class,
							projectionClass = BankAccount::class
						),
					),
				)
			)
		)
		assertEquals(actual = raptor.context.aggregateStore, expected = store)
	}
}
