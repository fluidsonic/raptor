package io.fluidsonic.raptor


@RaptorDsl
interface RaptorComponentSet<out Component : RaptorComponent> {

	@RaptorDsl
	fun forEach(action: Component.() -> Unit)


	// FIXME improve naming & nesting of ops
	// FIXME unit-test
	companion object {

		@RaptorDsl
		fun <Component : RaptorComponent> filter(
			set: RaptorComponentSet<Component>,
			filter: (component: Component) -> Boolean
		): RaptorComponentSet<Component> =
			RaptorComponentSet { action ->
				set.forEach {
					if (filter(this))
						action()
				}
			}


		@RaptorDsl
		fun <Component : RaptorComponent, TransformedComponent : RaptorComponent> map(
			set: RaptorComponentSet<Component>,
			transform: Component.() -> RaptorComponentSet<TransformedComponent>
		): RaptorComponentSet<TransformedComponent> {
			val actions: MutableList<TransformedComponent.() -> Unit> = mutableListOf()
			val transforms: MutableList<RaptorComponentSet<TransformedComponent>> = mutableListOf()

			set.forEach {
				val transformed = transform()

				transforms += transformed

				for (index in actions.indices)
					transformed.forEach(actions[index])
			}

			return RaptorComponentSet { action ->
				actions += action

				for (index in transforms.indices)
					transforms[index].forEach(action)
			}
		}
//
//
//		fun <Component : RaptorComponent> new(configure: (configure: Component.() -> Unit) -> Unit) =
//			object : RaptorComponentSet<Component> {
//
//				override fun invoke(configure: Component.() -> Unit) {
//					configure(configure)
//				}
//			}
//
//
//		fun <Component : RaptorComponent> of(component: Component) =
//			object : RaptorComponentSet<Component> {
//
//				override fun invoke(configure: Component.() -> Unit) {
//					configure(component)
//				}
//			}
	}
}


@RaptorDsl
@Suppress("FunctionName")
fun <Component : RaptorComponent> RaptorComponentSet(forEach: (action: Component.() -> Unit) -> Unit): RaptorComponentSet<Component> =
	object : RaptorComponentSet<Component> {

		override fun forEach(action: Component.() -> Unit) {
			forEach(action)
		}
	}


@RaptorDsl
operator fun <Component : RaptorComponent> RaptorComponentSet<Component>.invoke(action: Component.() -> Unit) =
	forEach(action)
