package io.fluidsonic.raptor


@RaptorDsl
interface RaptorComponentSet<Component : RaptorComponent<Component>> {

	@RaptorDsl
	fun forEach(action: Component.() -> Unit)


	companion object {

		// FIXME improve naming & nesting
		@RaptorDsl
		fun <Component : RaptorComponent<Component>, TransformedComponent : RaptorComponent<TransformedComponent>> map(
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


		// FIXME
//		fun <Component : RaptorComponent<Component>> filter(config: RaptorComponentSet<Component>, filter: (component: Component) -> Boolean) =
//			object : RaptorComponentSet<Component> {
//
//				override fun invoke(configure: Component.() -> Unit) {
//					config {
//						if (filter(this))
//							configure()
//					}
//				}
//			}
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
fun <Component : RaptorComponent<Component>> RaptorComponentSet(forEach: (action: Component.() -> Unit) -> Unit): RaptorComponentSet<Component> =
	object : RaptorComponentSet<Component> {

		override fun forEach(action: Component.() -> Unit) {
			forEach(action)
		}
	}


@RaptorDsl
operator fun <Component : RaptorComponent<Component>> RaptorComponentSet<Component>.invoke(action: Component.() -> Unit) =
	forEach(action)
