package io.fluidsonic.raptor


public interface RaptorComponentRegistry {

	public val parent: RaptorComponentRegistry?

	public fun <Component : RaptorComponent<out Component>> all(key: RaptorComponentKey<out Component>): RaptorComponentSet<Component>
	public fun isEmpty(): Boolean
	public fun <Component : RaptorComponent<out Component>> oneOrNull(key: RaptorComponentKey<out Component>): Component?
	public fun <Component : RaptorComponent<out Component>> many(key: RaptorComponentKey<out Component>): List<Component>

	@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE") // FIXME
	@kotlin.internal.LowPriorityInOverloadResolution
	public fun <Component : RaptorComponent<in Component>> register(key: RaptorComponentKey<in Component>, component: Component): Component
	override fun toString(): String


	public companion object {

		public fun default(parent: RaptorComponentRegistry? = null): RaptorComponentRegistry =
			DefaultComponentRegistry(parent = parent)
	}
}


public fun <Component : RaptorComponent<Component>> RaptorComponentRegistry.all(
	key: RaptorComponentKey<Component>,
	action: Component.() -> Unit,
) {
	all(key = key).all(action)
}


public fun <Component : RaptorComponent<Component>> RaptorComponentRegistry.one(key: RaptorComponentKey<Component>): Component =
	oneOrNull(key) ?: error("Expected a component to be registered for key '$key'.")


public inline fun <Component : RaptorComponent<Component>> RaptorComponentRegistry.oneOrRegister(
	key: RaptorComponentKey<Component>,
	create: () -> Component,
): Component =
	oneOrNull(key) ?: register(key, create)


@Deprecated(message = "Create component directly.", replaceWith = ReplaceWith("this.register(key, create())"))
public inline fun <Component : RaptorComponent<Component>> RaptorComponentRegistry.register(
	key: RaptorComponentKey<Component>,
	create: () -> Component,
): Component =
	create().also { register(key, it) }


public val RaptorComponentRegistry.root: RaptorComponentRegistry
	get() = parent?.root ?: this


@RaptorDsl
public val RaptorComponent<*>.componentRegistry: RaptorComponentRegistry
	get() = registration.registry
