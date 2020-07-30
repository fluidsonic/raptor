package io.fluidsonic.raptor


@RaptorDsl
@Suppress("NON_PUBLIC_PRIMARY_CONSTRUCTOR_OF_INLINE_CLASS")
inline class RaptorComponentAuthoring<Component : RaptorComponent>(
	@RaptorDsl val authoredSet: RaptorComponentSet<Component>
)


@RaptorDsl
@Suppress("unused")
fun <Component : RaptorComponent> RaptorComponentAuthoring<*>.componentSet(
	configure: (action: Component.() -> Unit) -> Unit
): RaptorComponentSet<Component> =
	object : RaptorComponentSet<Component> {

		override fun configure(action: Component.() -> Unit) {
			configure(action)
		}
	}


@RaptorDsl
fun <Component : RaptorComponent> RaptorComponentAuthoring<Component>.filter(
	filter: (component: Component) -> Boolean
): RaptorComponentSet<Component> =
	componentSet { action ->
		authoredSet {
			if (filter(this))
				action()
		}
	}


@RaptorDsl
fun <Component : RaptorComponent, TransformedComponent : RaptorComponent> RaptorComponentAuthoring<Component>.map(
	transform: Component.() -> RaptorComponentSet<TransformedComponent>
): RaptorComponentSet<TransformedComponent> {
	val actions: MutableList<TransformedComponent.() -> Unit> = mutableListOf()
	val transforms: MutableList<RaptorComponentSet<TransformedComponent>> = mutableListOf()

	authoredSet {
		val transformed = transform()

		transforms += transformed

		for (index in actions.indices)
			transformed.configure(actions[index])
	}

	return componentSet { action ->
		actions += action

		for (index in transforms.indices)
			transforms[index].configure(action)
	}
}


@RaptorDsl
inline fun <Component : RaptorComponent, Result> RaptorComponentSet<Component>.withComponentAuthoring(
	action: RaptorComponentAuthoring<Component>.() -> Result
) =
	RaptorComponentAuthoring(authoredSet = this).run(action)
