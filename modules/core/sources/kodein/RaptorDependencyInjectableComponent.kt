package io.fluidsonic.raptor

import org.kodein.di.*


@Raptor.Dsl3
interface RaptorDependencyInjectableComponent : RaptorComponent


@Raptor.Dsl3
fun RaptorDependencyInjectableComponent.kodein(configuration: Kodein.Builder.() -> Unit)

fun

interface KodeinBoundary : RaptorComponent {

	@Raptor.Dsl3
	fun kodein(configure: Kodein.Builder.() -> Unit)
}
