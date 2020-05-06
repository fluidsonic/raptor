package io.fluidsonic.raptor


// FIXME clean up
@Raptor.Dsl3
interface RaptorComponentConfig<out Component : RaptorComponent> : (Component.() -> Unit) -> Unit {

	@Raptor.Dsl3
	override operator fun invoke(configure: Component.() -> Unit)


	companion object {

		fun <Component : RaptorComponent> filter(config: RaptorComponentConfig<Component>, filter: (component: Component) -> Boolean) =
			object : RaptorComponentConfig<Component> {

				override fun invoke(configure: Component.() -> Unit) {
					config {
						if (filter(this))
							configure()
					}
				}
			}


		fun <Component : RaptorComponent> new(configure: (configure: Component.() -> Unit) -> Unit) =
			object : RaptorComponentConfig<Component> {

				override fun invoke(configure: Component.() -> Unit) {
					configure(configure)
				}
			}


		fun <Component : RaptorComponent> of(component: Component) =
			object : RaptorComponentConfig<Component> {

				override fun invoke(configure: Component.() -> Unit) {
					configure(component)
				}
			}
	}
}
