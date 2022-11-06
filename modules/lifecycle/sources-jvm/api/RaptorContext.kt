package io.fluidsonic.raptor.lifecycle

import io.fluidsonic.raptor.*


internal val RaptorContext.lifecycle: DefaultLifecycle
	get() = properties[Keys.lifecycleProperty] ?: throw RaptorPluginNotInstalledException(RaptorLifecyclePlugin)
