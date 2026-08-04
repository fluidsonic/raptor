import io.fluidsonic.raptor.domain.*


sealed interface CounterCommand : RaptorAggregateCommand<CounterNumber> {

	object Create : CounterCommand
	object Increment : CounterCommand
}
