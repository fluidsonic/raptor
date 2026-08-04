import io.fluidsonic.raptor.domain.*


sealed interface CounterChange : RaptorAggregateChange<CounterNumber> {

	object Created : CounterChange
	object Incremented : CounterChange
}
