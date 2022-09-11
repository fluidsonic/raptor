package io.fluidsonic.raptor


public interface RaptorPropertySet {

	public operator fun <Value : Any> get(key: RaptorPropertyKey<out Value>): Value?
	public fun isEmpty(): Boolean
	override fun toString(): String


	public companion object {

		public fun empty(): RaptorPropertySet =
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
	private val fallback: RaptorPropertySet,
) : RaptorPropertySet {

	override fun <Value : Any> get(key: RaptorPropertyKey<out Value>): Value? =
		set[key] ?: fallback[key]


	override fun isEmpty() =
		set.isEmpty() && fallback.isEmpty()


	override fun toString() =
		set.toString() // FIXME add fallback
}


public fun RaptorPropertySet.withFallback(fallback: RaptorPropertySet): RaptorPropertySet {
	check(fallback !== this) { "A property set cannot fall back to itself." }

	return when {
		this === EmptyRaptorPropertySet -> fallback
		fallback === EmptyRaptorPropertySet -> this
		else -> FallbackRaptorPropertySet(set = this, fallback = fallback)
	}

}
