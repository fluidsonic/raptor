package io.fluidsonic.raptor

import org.kodein.di.*


internal object DKodeinRaptorPropertyKey : RaptorPropertyKey<DKodein> {

	override fun toString() = "dkodein"
}
