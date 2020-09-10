package io.fluidsonic.raptor


// FIXME raptor-model module?
public open class InvalidValueException : Exception("Invalid value.") {

	public companion object;


	public class ForbiddenCharacter(
		public val input: String,
		public val index: Int,
	) : InvalidValueException() {

		init {
			require(index in message.indices) { "Index $index is out of bounds for input of length ${input.length}: $input" }
		}


		override val message: String
			get() = "Input contains forbidden character '${input[index]}' (0x${input[index].toInt().toString(16).toUpperCase()}) at index $index: $input"
	}


	public class TooLarge private constructor(
		public val input: Comparable<*>,
		public val maximum: Comparable<*>,
	) : InvalidValueException() {

		override val message: String
			get() = "Input must be at most $maximum: $input"


		public companion object {

			public operator fun <Input : Comparable<Input>> invoke(
				input: Input,
				maximum: Input,
			): TooLarge {
				require(input > maximum) { "'input' must be larger than maximum $maximum: $input" }

				return TooLarge(input = input, maximum = maximum)
			}
		}
	}


	public class TooLong(
		public val input: String,
		public val maximumLength: Int,
	) : InvalidValueException() {

		init {
			require(maximumLength >= 0) { "'maximumLength' must be at least 0: $maximumLength" }
			require(input.length > maximumLength) { "'input' must be longer than maximum $maximumLength: $input" }
		}


		override val message: String
			get() = when (maximumLength) {
				0 -> "Input must be empty."
				else -> "Input must have a maximum length of $maximumLength but is ${input.length}: $input"
			}
	}


	public class TooShort(
		public val input: String,
		public val minimumLength: Int,
	) : InvalidValueException() {

		init {
			require(minimumLength >= 1) { "'minimumLength' must be at least 1: $minimumLength" }
			require(input.length < minimumLength) { "'input' must be shorter than minimum $minimumLength: $input" }
		}


		override val message: String
			get() = when (minimumLength) {
				1 -> "Input must not be empty."
				else -> "Input have a minimum length of $minimumLength but is ${input.length}: $input"
			}
	}


	public class TooSmall private constructor(
		public val input: Comparable<*>,
		public val minimum: Comparable<*>,
	) : InvalidValueException() {

		override val message: String
			get() = "Input must be at least $minimum: $input"


		public companion object {

			public operator fun <Input : Comparable<Input>> invoke(
				input: Input,
				minimum: Input,
			): TooSmall {
				require(input < minimum) { "'input' must be smaller than minimum $minimum: $input" }

				return TooSmall(input = input, minimum = minimum)
			}
		}
	}
}


@RaptorDsl
public fun invalidValueError(): Nothing =
	throw InvalidValueException()
