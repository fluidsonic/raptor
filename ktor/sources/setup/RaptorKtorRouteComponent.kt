package io.fluidsonic.raptor

import io.ktor.routing.*
import org.kodein.di.*


interface RaptorKtorRouteComponent : RaptorComponent.Taggable {

	fun install(feature: RaptorKtorRouteFeature)
}


@Raptor.Dsl3
fun RaptorConfigurable<RaptorKtorRouteComponent>.custom(config: Route.() -> Unit) {
	TODO()
}


@Raptor.Dsl3
fun RaptorConfigurable<RaptorKtorRouteComponent>.kodein(config: Kodein.Builder.() -> Unit) { // FIXME make own API
	forEachComponent {
		TODO()
	}
}


@Raptor.Dsl3
fun RaptorConfigurable<RaptorKtorRouteComponent>.newRoute(
	path: String,
	vararg tags: Any = emptyArray(),
	config: RaptorConfigurable<RaptorKtorRouteComponent>.() -> Unit
) {
	TODO()
	//routes.create(path = path, tags = *tags, config = config)
}


@Raptor.Dsl3
val RaptorConfigurable<RaptorKtorRouteComponent>.routes
	get() = raptorSetupContext.getOrCreateComponentCollection<RaptorKtorRouteComponent>()


@Raptor.Dsl3
fun RaptorConfigurable<RaptorKtorRouteComponent>.wrap(wrapper: Route.(next: Route.() -> Unit) -> Route) {
	TODO()
}
