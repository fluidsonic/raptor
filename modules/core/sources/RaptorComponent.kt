package io.fluidsonic.raptor


@RaptorDsl
interface RaptorComponent {

	val extensions: RaptorExtensionSet


	companion object


	@RaptorDsl
	abstract class Base<out Self : Typed<Self>> : Typed<Self> {

		final override val extensions: RaptorExtensionSet = DefaultRaptorExtensionSet()


		@RaptorDsl
		@Suppress("UNCHECKED_CAST")
		final override fun forEach(action: Self.() -> Unit) {
			(this as Self).action()
		}
	}


	@RaptorDsl
	class Simple : Base<Simple>()


	interface Typed<out Self : Typed<Self>> : RaptorComponent, RaptorComponentSet<Self>
}
