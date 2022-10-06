package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*


public val RaptorContext.ktor: RaptorKtor
	get() = ktorInternal ?: throw RaptorPluginNotInstalledException(RaptorKtorPlugin)


internal val RaptorContext.ktorInternal: RaptorKtorInternal?
	get() = properties[Keys.ktorProperty]
