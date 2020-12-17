package io.fluidsonic.raptor

import freemarker.template.*


internal object FreemarkerRaptorPropertyKey : RaptorPropertyKey<Configuration> {

	override fun toString() = "freemarker configuration"
}
