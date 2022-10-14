package io.fluidsonic.raptor.lifecycle

import io.fluidsonic.raptor.*


public val Raptor.lifecycle: RaptorLifecycle
	get() = context.lifecycle
