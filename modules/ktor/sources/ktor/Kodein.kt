package io.fluidsonic.raptor

import org.kodein.di.*


fun Kodein.configString(path: String) =
	newInstance { configString(path) }
