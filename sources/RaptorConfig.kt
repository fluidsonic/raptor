package io.fluidsonic.raptor

import org.kodein.di.*


internal class RaptorConfig(
	val kodeinModule: Kodein.Module,
	val serverConfig: RaptorServerConfig?,
	val startCallbacks: List<suspend RaptorScope.() -> Unit>,
	val stopCallbacks: List<suspend RaptorScope.() -> Unit>
)
