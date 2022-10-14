import BankAccountChange.*
import BankAccountCommand.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.domain.*
import io.fluidsonic.raptor.lifecycle.*
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

		val configuration = raptor.context.plugins.domain
		assertEquals(actual = configuration.aggregates.definitions, expected = RaptorAggregateDefinitions(setOf(
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
				projectionDefinition = RaptorAggregateProjectionDefinition(
					factory = ::BankAccountProjector,
					idClass = BankAccountNumber::class,
					projectionClass = BankAccount::class
				),
			),
		)))
		assertEquals(actual = configuration.aggregates.eventFactory, expected = eventFactory)
		assertEquals(actual = configuration.aggregates.store, expected = store)
	}
}
