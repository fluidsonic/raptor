package io.fluidsonic.raptor


@Raptor.Dsl3
interface RaptorComponentSet<Component : RaptorComponent<Component>> : (Component.() -> Unit) -> Unit {

	@Raptor.Dsl3
	override operator fun invoke(configure: Component.() -> Unit)

	// FIXME
//	companion object {
//
//		fun <Component : RaptorComponent> filter(config: RaptorComponentSet<Component>, filter: (component: Component) -> Boolean) =
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
//	}
}
