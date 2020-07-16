package io.fluidsonic.raptor

import org.kodein.di.*
import org.kodein.di.erased.*


@RaptorDsl
val DKodein.raptorContext
	get() = instance<RaptorContext>()
