package io.fluidsonic.raptor.d2.kodein

import io.ktor.config.*
import org.kodein.di.*
import org.kodein.di.erased.*


fun DKodein.configString(path: String) =
	instance<ApplicationConfig>().property(path).getString()
