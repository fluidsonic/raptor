package io.fluidsonic.raptor.service2

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*


@RaptorDsl
context(di: RaptorDI)
public inline fun <reified Value> di(): Value =
	di.get()
