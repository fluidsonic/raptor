@file:JvmName("RaptorConfigurationComponent+hocon")

package io.fluidsonic.raptor


@RaptorDsl
fun RaptorComponentSet<RaptorConfigurationComponent>.hocon(resourcePath: String) {
	append(HoconRaptorConfiguration(resourcePath))
}
