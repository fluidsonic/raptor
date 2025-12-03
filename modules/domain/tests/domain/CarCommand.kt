import io.fluidsonic.raptor.domain.*


sealed interface CarCommand : RaptorAggregateCommand<CarNumber> {

	data object Build : CarCommand
	data object Crash : CarCommand
	data object Deliver : CarCommand
	data class Drive(val distance: Int) : CarCommand
}
