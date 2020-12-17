package io.fluidsonic.raptor


@RaptorDsl
public interface RaptorComponent {

	@RaptorDsl
	public val extensions: RaptorComponentExtensionSet

	public fun RaptorComponentConfigurationEndScope.onConfigurationEnded(): Unit = Unit
	public fun RaptorComponentConfigurationStartScope.onConfigurationStarted(): Unit = Unit


	public companion object


	@RaptorDsl
	public abstract class Default<out Self : Typed<Self>> : Typed<Self> { // FIXME rn to Base?

		@RaptorDsl
		final override val extensions: RaptorComponentExtensionSet = DefaultRaptorComponentExtensionSet()

		@RaptorDsl
		@Suppress("UNCHECKED_CAST")
		final override fun configure(action: Self.() -> Unit) {
			(this as Self).action()
		}
	}


	public interface Typed<out Self : Typed<Self>> : RaptorComponent, RaptorComponentSet<Self>
}
