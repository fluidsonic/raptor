// FIXME migrate to v2
//package io.fluidsonic.raptor
//
//
//@JvmInline
//@RaptorDsl
//public value class RaptorComponentAuthoring<Component : RaptorComponent>(
//	@RaptorDsl public val authoredSet: RaptorComponentSet<Component>,
//)
//
//
//@RaptorDsl
//@Suppress("unused")
//public fun <Component : RaptorComponent> RaptorComponentAuthoring<*>.componentSet(
//	configure: (action: Component.() -> Unit) -> Unit,
//): RaptorComponentSet<Component> =
//	object : RaptorComponentSet<Component> {
//
//		override fun configure(action: Component.() -> Unit) {
//			configure(action)
//		}
//	}
//
//
//@RaptorDsl
//public fun <Component : RaptorComponent> RaptorComponentAuthoring<Component>.filter(
//	filter: (component: Component) -> Boolean,
//): RaptorComponentSet<Component> =
//	componentSet { action ->
//		authoredSet {
//			if (filter(this))
//				action()
//		}
//	}
//
//
//@RaptorDsl
//public fun <Component : RaptorComponent, TransformedComponent : RaptorComponent> RaptorComponentAuthoring<Component>.map(
//	transform: Component.() -> RaptorComponentSet<TransformedComponent>,
//): RaptorComponentSet<TransformedComponent> {
//	val actions: MutableList<TransformedComponent.() -> Unit> = mutableListOf()
//	val transforms: MutableList<RaptorComponentSet<TransformedComponent>> = mutableListOf()
//
//	authoredSet {
//		val transformed = transform()
//
//		transforms += transformed
//
//		for (index in actions.indices)
//			transformed.configure(actions[index])
//	}
//
//	return componentSet { action ->
//		actions += action
//
//		for (index in transforms.indices)
//			transforms[index].configure(action)
//	}
//}
//
//
//@RaptorDsl
//public inline fun <Component : RaptorComponent, Result> RaptorComponentSet<Component>.withComponentAuthoring(
//	action: RaptorComponentAuthoring<Component>.() -> Result,
//): Result =
//	RaptorComponentAuthoring(authoredSet = this).run(action)
