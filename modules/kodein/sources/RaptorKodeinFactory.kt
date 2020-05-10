package io.fluidsonic.raptor

import org.kodein.di.*


interface RaptorKodeinFactory {

	fun createKodein(context: RaptorContext, configuration: RaptorKodeinBuilder.() -> Unit = {}): Kodein

	companion object
}
