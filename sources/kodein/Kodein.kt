package io.fluidsonic.raptor.d2.kodein

import org.kodein.di.*


fun Kodein.configString(path: String) =
	newInstance { configString(path) }
