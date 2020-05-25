// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("DKodein+Configuration")

package io.fluidsonic.raptor

import org.kodein.di.*
import org.kodein.di.erased.*


val DKodein.raptorConfiguration
	get() = instance<RaptorConfiguration>()
