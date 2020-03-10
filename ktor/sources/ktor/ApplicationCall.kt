package io.fluidsonic.raptor

import io.ktor.application.*
import io.ktor.util.pipeline.*
import org.kodein.di.*


// FIXME rework
val ApplicationCall.dkodein: DKodein
	get() = TODO()


// FIXME rework
val PipelineContext<Unit, ApplicationCall>.dkodein: DKodein
	get() = TODO()
