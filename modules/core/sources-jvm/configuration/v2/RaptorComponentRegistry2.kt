package io.fluidsonic.raptor


public interface RaptorComponentRegistry2 {

	public val parent: RaptorComponentRegistry2?

	public fun <Component : RaptorComponent2> all(key: RaptorComponentKey2<out Component>): RaptorComponentSet2<Component>
	public fun isEmpty(): Boolean
	public fun <Component : RaptorComponent2> oneOrNull(key: RaptorComponentKey2<out Component>): Component?
	public fun <Component : RaptorComponent2> many(key: RaptorComponentKey2<out Component>): List<Component>
	public fun <Component : RaptorComponent2> register(key: RaptorComponentKey2<in Component>, component: Component)
	override fun toString(): String


	public companion object {

		public fun default(parent: RaptorComponentRegistry2? = null): RaptorComponentRegistry2 =
			DefaultRaptorComponentRegistry2(parent = parent)
	}
}


public fun <Component : RaptorComponent2> RaptorComponentRegistry2.all(
	key: RaptorComponentKey2<Component>,
	action: Component.() -> Unit,
) {
	all(key = key).all(action)
}


public fun <Component : RaptorComponent2> RaptorComponentRegistry2.one(key: RaptorComponentKey2<Component>): Component =
	oneOrNull(key) ?: error("Expected a component to be registered for key '$key'.")


public inline fun <Component : RaptorComponent2> RaptorComponentRegistry2.oneOrRegister(
	key: RaptorComponentKey2<Component>,
	create: () -> Component,
): Component =
	oneOrNull(key) ?: create().also { register(key, it) }


public val RaptorComponentRegistry2.root: RaptorComponentRegistry2
	get() = parent?.root ?: this


@RaptorDsl
public val RaptorComponent.componentRegistry2: RaptorComponentRegistry2
	get() = extensions[RaptorComponentRegistryExtensionKey2]
		?: error("Cannot access the component registry of a component that hasn't been registered yet.")
