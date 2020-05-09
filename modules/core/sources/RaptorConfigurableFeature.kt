package io.fluidsonic.raptor

import kotlin.reflect.*


interface RaptorConfigurableFeature<RootComponent : RaptorComponent<RootComponent>> {

	fun RaptorFeatureFinalizationScope.finalizeConfigurable(rootComponent: RootComponent) = Unit
	fun RaptorFeatureInstallationScope.installConfigurable(): RootComponent


	companion object
}


@RaptorDsl
inline fun <Feature : RaptorConfigurableFeature<RootComponent>, reified RootComponent : RaptorComponent<RootComponent>> RaptorFeatureInstallationTarget.install(
	feature: Feature,
	noinline configure: RootComponent.() -> Unit = {}
) {
	install(feature = feature, rootComponentType = RootComponent::class, configure = configure)
}


@RaptorDsl
inline fun <reified Component : RaptorComponent<Component>> RaptorFeatureFinalizationScope.component(): Component =
	component(Component::class)


@RaptorDsl
fun <Component : RaptorComponent<Component>> RaptorFeatureFinalizationScope.component(type: KClass<Component>): Component =
	components(type)
		.also { components ->
			when (components.size) {
				0 -> error("Expected a single component of type '${type.qualifiedName}' but none was registered.")
				1 -> Unit
				else -> error(
					"Expected a single component of type '${type.qualifiedName}' but ${components.size} have been registered:\n\t" +
						components.joinToString("\n\t")
				)
			}
		}
		.first()


@RaptorDsl
inline fun <reified Component : RaptorComponent<Component>> RaptorFeatureFinalizationScope.components(): Collection<Component> =
	components(Component::class)
