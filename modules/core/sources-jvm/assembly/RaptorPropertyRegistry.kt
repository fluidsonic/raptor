package io.fluidsonic.raptor


public interface RaptorPropertyRegistry {

	public fun <Value : Any> register(key: RaptorPropertyKey<in Value>, value: Value)
	public fun toSet(): RaptorPropertySet
	override fun toString(): String


	public companion object {

		public fun default(): RaptorPropertyRegistry =
			DefaultPropertyRegistry()
	}
}
