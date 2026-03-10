import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.domain.*
import io.fluidsonic.raptor.lifecycle.*
import io.fluidsonic.time.*
import kotlin.test.*
import kotlinx.coroutines.test.*
import kotlinx.datetime.*
import org.slf4j.*
import org.slf4j.helpers.*


class AssemblyTests {

	@Test
	fun testNewAggregate() = runTest {
		val clock = ManualClock()
		val hook = MessageCollectionHook()
		val store = TestAggregateStore()

		val raptor = raptor {
			install(RaptorDIPlugin)
			install(RaptorDomainPlugin)
			install(RaptorLifecyclePlugin)

			di {
				provide<Clock>(clock)
				provide<Logger>(NOPLogger.NOP_LOGGER)
				provide<MessageCollectionHook>(hook)
			}

			domain.aggregates {
				hook { get<MessageCollectionHook>() }
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
				)
			)
		)
		assertEquals(actual = raptor.context.aggregateStore, expected = store)

		with(raptor.context.asScope()) {
			val id = BankAccountNumber("1")

			clock.set(Timestamp.fromEpochSeconds(1))
			raptor.lifecycle.startIn(this@runTest)
			commandExecutor.execute(id, BankAccountCommand.Create("Marc"))

			val bankAccount = projectionLoader<BankAccount, BankAccountNumber>().fetchOrNull(id)
			assertEquals(
				actual = bankAccount,
				expected = BankAccount(
					amount = 0,
					id = id,
					label = null,
					owner = "Marc"
				)
			)

			assertEquals<List<RaptorAggregateStreamMessage<*, *>>>(
				actual = hook.messages,
				expected = listOf(
					RaptorAggregateStreamMessage.Loaded,
					RaptorAggregateEventBatch(
						aggregateId = id,
						events = listOf(
							RaptorAggregateEvent(
								aggregateId = id,
								change = BankAccountChange.Created("Marc"),
								id = RaptorAggregateEventId(1),
								timestamp = clock.now(),
								version = 1,
							),
						),
						version = 1,
					)
				),
			)
			assertEquals<List<RaptorAggregateProjectionStreamMessage<*, *, *>>>(
				actual = hook.projectionMessages,
				expected = listOf(
					RaptorAggregateProjectionStreamMessage.Loaded,
					RaptorAggregateProjectionEventBatch(
						events = listOf(
							RaptorAggregateProjectionEvent(
								change = BankAccountChange.Created("Marc"),
								id = RaptorAggregateEventId(1),
								projection = BankAccount(amount = 0, id = id, label = null, owner = "Marc"),
								timestamp = clock.now(),
								version = 1,
							),
						),
						projectionId = id,
						version = 1,
					)
				)
			)

			raptor.lifecycle.stop()
		}
	}
}
