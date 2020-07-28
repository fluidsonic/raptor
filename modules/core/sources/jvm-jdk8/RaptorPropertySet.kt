package io.fluidsonic.raptor


interface RaptorPropertySet {

	operator fun <Value : Any> get(key: RaptorPropertyKey<out Value>): Value?
	fun isEmpty(): Boolean
	override fun toString(): String


	companion object {

		fun empty(): RaptorPropertySet =
			EmptyRaptorPropertySet
	}
}


private object EmptyRaptorPropertySet : RaptorPropertySet {

	override fun <Value : Any> get(key: RaptorPropertyKey<out Value>): Nothing? = null


	override fun isEmpty() =
		true


	override fun toString() =
		"[property set] -> (empty)"
}


private class FallbackRaptorPropertySet(
	private val set: RaptorPropertySet,
	private val fallback: RaptorPropertySet
) : RaptorPropertySet {

	override fun <Value : Any> get(key: RaptorPropertyKey<out Value>): Value? =
		set[key] ?: fallback[key]


	override fun isEmpty() =
		set.isEmpty() && fallback.isEmpty()


	override fun toString() =
		set.toString() // FIXME add fallback
}


fun RaptorPropertySet.withFallback(fallback: RaptorPropertySet): RaptorPropertySet {
	check(fallback !== this) { "A property set cannot fall back to itself." }

	return when {
		this === EmptyRaptorPropertySet -> fallback
		fallback === EmptyRaptorPropertySet -> this
		else -> FallbackRaptorPropertySet(set = this, fallback = fallback)
	}

}
