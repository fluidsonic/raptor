import io.fluidsonic.raptor.domain.*


sealed interface CarChange : RaptorAggregateChange<CarNumber> {

	data object Built : CarChange
	data object Crashed : CarChange
	data object Delivered : CarChange
	data class Driven(val distance: Int) : CarChange
}
