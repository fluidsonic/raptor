package io.fluidsonic.raptor


@RaptorDsl
interface RaptorComponent<Self : RaptorComponent<Self>> : RaptorComponentSet<Self> {

	@RaptorDsl
	@Suppress("UNCHECKED_CAST")
	override fun forEach(action: Self.() -> Unit) {
		(this as Self).action()
	}


	companion object


	class Simple : RaptorComponent<Simple>
}
