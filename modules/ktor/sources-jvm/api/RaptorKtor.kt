package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*


public interface RaptorKtor {

	public val servers: Collection<RaptorKtorServer>
}


public val RaptorContext.ktor: RaptorKtor
	get() = ktorInternal ?: throw RaptorFeatureNotInstalledException(RaptorKtorFeature)
