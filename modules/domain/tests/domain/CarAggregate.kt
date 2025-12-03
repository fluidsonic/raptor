import CarChange.*
import CarCommand.*
import io.fluidsonic.raptor.domain.*


class CarAggregate(
	override val id: CarNumber,
) : RaptorAggregate<CarNumber, CarCommand, CarChange> {

	private var status: CarStatus? = null
	private var distanceDriven = 0


	override fun copy() =
		CarAggregate(id).also { copy ->
			copy.status = status
			copy.distanceDriven = distanceDriven
		}


	override fun execute(command: CarCommand): List<CarChange> =
		listOf(
			when (command) {
				is Build -> execute(command)
				is Crash -> execute(command)
				is Deliver -> execute(command)
				is Drive -> execute(command)
			}
		)


	private fun execute(command: Build): Built {
		check(status == null) { "Already built." }

		return Built
	}


	private fun execute(@Suppress("UNUSED_PARAMETER") command: Crash): Crashed {
		check(status == CarStatus.driven) { "Cannot crash car without driving it." }

		return Crashed
	}


	private fun execute(command: Deliver): Delivered {
		check(status == CarStatus.built) { "Not yet built or already delivered." }

		return Delivered
	}


	private fun execute(command: Drive): Driven {
		check(status == CarStatus.delivered || status == CarStatus.driven) { "Cannot drive this car." }

		return Driven(distance = command.distance)
	}


	override fun handle(change: CarChange) {
		when (change) {
			is Built -> handle(change)
			is Crashed -> handle(change)
			is Delivered -> handle(change)
			is Driven -> handle(change)
		}
	}


	private fun handle(@Suppress("UNUSED_PARAMETER") event: Built) {
		status = CarStatus.built
	}


	private fun handle(event: Crashed) {
		status = CarStatus.crashed
	}


	private fun handle(@Suppress("UNUSED_PARAMETER") event: Delivered) {
		status = CarStatus.delivered
	}


	private fun handle(event: Driven) {
		distanceDriven += event.distance
		status = CarStatus.driven
	}


	override fun toString() =
		"CarAggregate(id=$id, distanceDriven=$distanceDriven, status=$status)"


	private enum class CarStatus {
		crashed,
		delivered,
		driven,
		built,
	}
}
