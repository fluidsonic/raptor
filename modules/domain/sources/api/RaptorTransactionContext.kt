package io.fluidsonic.raptor.cqrs

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*


internal val RaptorTransactionContext.aggregateManager: RaptorAggregateManager
	get() = properties[Keys.aggregateManagerProperty] ?: throw RaptorPluginNotInstalledException(RaptorDomainPlugin)
