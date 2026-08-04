import io.fluidsonic.raptor.domain.*


data class Counter(
	override val id: CounterNumber,
	val value: Int,
) : RaptorAggregateProjection<CounterNumber>
