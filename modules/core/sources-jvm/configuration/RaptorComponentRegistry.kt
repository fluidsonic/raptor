package io.fluidsonic.raptor


public interface RaptorComponentRegistry {

	public val parent: RaptorComponentRegistry?

	public fun <Component : RaptorComponent> configure(key: RaptorComponentKey<out Component>): RaptorComponentSet<Component>
	public fun isEmpty(): Boolean
	public fun <Component : RaptorComponent> oneOrNull(key: RaptorComponentKey<out Component>): Component?
	public fun <Component : RaptorComponent> many(key: RaptorComponentKey<out Component>): List<Component>
	public fun <Component : RaptorComponent> register(key: RaptorComponentKey<in Component>, component: Component)
	override fun toString(): String


	public companion object {

		public fun default(parent: RaptorComponentRegistry? = null): RaptorComponentRegistry =
			DefaultRaptorComponentRegistry(parent = parent)
	}
}


// https://kotlinlang.slack.com/archives/C0B9K7EP2/p1588740913072200
public fun <Component : RaptorComponent> RaptorComponentRegistry.configure(
	key: RaptorComponentKey<Component>,
	action: Component.() -> Unit,
) {
	configure(key = key).configure(action)
}


public fun <Component : RaptorComponent> RaptorComponentRegistry.one(key: RaptorComponentKey<Component>): Component =
	oneOrNull(key) ?: error("Expected a component to be registered for key '$key'.")


public inline fun <Component : RaptorComponent> RaptorComponentRegistry.oneOrRegister(
	key: RaptorComponentKey<Component>,
	create: () -> Component,
): Component =
	oneOrNull(key) ?: create().also { register(key, it) }


public val RaptorComponentRegistry.root: RaptorComponentRegistry
	get() = parent?.root ?: this


@RaptorDsl
public val RaptorComponent.componentRegistry: RaptorComponentRegistry
	get() = extensions[RaptorComponentRegistryExtensionKey]
		?: error("Cannot access the component registry of a component that hasn't been registered yet.")
