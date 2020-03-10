package io.fluidsonic.raptor

import io.ktor.config.*
import org.kodein.di.*
import org.kodein.di.erased.*


// FIXME use different approach for config that isn't dependent on Ktor
fun DKodein.configString(path: String) =
	instance<ApplicationConfig>().property(path).getString()
