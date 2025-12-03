import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.domain.*
import io.fluidsonic.raptor.domain.memory.*
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
				individualStoreFactory(RaptorIndividualAggregateStoreFactory.memory())
				store(store)

				new(::BankAccountAggregate, "bank account") {
					project(::BankAccountProjector)

					command<BankAccountCommand.Create>()
					command<BankAccountCommand.Delete>()
					command<BankAccountCommand.Deposit>()
					command<BankAccountCommand.Label>()
					command<BankAccountCommand.Withdraw>()

					change<BankAccountChange.Created>("created")
					change<BankAccountChange.Deleted>("deleted")
					change<BankAccountChange.Deposited>("deposited")
					change<BankAccountChange.Labeled>("labeled")
					change<BankAccountChange.Withdrawn>("withdrawn")
				}

				new(::CarAggregate, "car", individual = true) {
					command<CarCommand.Build>()
					command<CarCommand.Crash>()
					command<CarCommand.Deliver>()
					command<CarCommand.Drive>()

					change<CarChange.Built>("built")
					change<CarChange.Crashed>("crashed")
					change<CarChange.Delivered>("delivered")
					change<CarChange.Driven>("driven")
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
							RaptorAggregateChangeDefinition(discriminator = "created", changeClass = BankAccountChange.Created::class),
							RaptorAggregateChangeDefinition(discriminator = "deleted", changeClass = BankAccountChange.Deleted::class),
							RaptorAggregateChangeDefinition(discriminator = "deposited", changeClass = BankAccountChange.Deposited::class),
							RaptorAggregateChangeDefinition(discriminator = "labeled", changeClass = BankAccountChange.Labeled::class),
							RaptorAggregateChangeDefinition(discriminator = "withdrawn", changeClass = BankAccountChange.Withdrawn::class),
						),
						commandClass = BankAccountCommand::class,
						commandDefinitions = setOf(
							RaptorAggregateCommandDefinition(commandClass = BankAccountCommand.Create::class),
							RaptorAggregateCommandDefinition(commandClass = BankAccountCommand.Delete::class),
							RaptorAggregateCommandDefinition(commandClass = BankAccountCommand.Deposit::class),
							RaptorAggregateCommandDefinition(commandClass = BankAccountCommand.Label::class),
							RaptorAggregateCommandDefinition(commandClass = BankAccountCommand.Withdraw::class),
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
					RaptorAggregateDefinition(
						aggregateClass = CarAggregate::class,
						changeClass = CarChange::class,
						changeDefinitions = setOf(
							RaptorAggregateChangeDefinition(discriminator = "built", changeClass = CarChange.Built::class),
							RaptorAggregateChangeDefinition(discriminator = "crashed", changeClass = CarChange.Crashed::class),
							RaptorAggregateChangeDefinition(discriminator = "delivered", changeClass = CarChange.Delivered::class),
							RaptorAggregateChangeDefinition(discriminator = "driven", changeClass = CarChange.Driven::class),
						),
						commandClass = CarCommand::class,
						commandDefinitions = setOf(
							RaptorAggregateCommandDefinition(commandClass = CarCommand.Build::class),
							RaptorAggregateCommandDefinition(commandClass = CarCommand.Crash::class),
							RaptorAggregateCommandDefinition(commandClass = CarCommand.Deliver::class),
							RaptorAggregateCommandDefinition(commandClass = CarCommand.Drive::class),
						),
						discriminator = "car",
						factory = RaptorAggregateFactory(::CarAggregate),
						idClass = CarNumber::class,
						isIndividual = true,
						projectionDefinition = null,
					),
				)
			)
		)
		assertEquals(actual = raptor.context.aggregateStore, expected = store)
	}
}
