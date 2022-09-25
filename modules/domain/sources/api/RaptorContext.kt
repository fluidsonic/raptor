package io.fluidsonic.raptor.cqrs

import io.fluidsonic.raptor.*


// FIXME tx sub managers
internal val RaptorContext.aggregateManager: RaptorAggregateManager
	get() = properties[Keys.aggregateManagerProperty] ?: throw RaptorFeatureNotInstalledException(RaptorDomainFeature)


public val RaptorContext.domain: RaptorDomain
	get() = properties[Keys.domainProperty] ?: throw RaptorFeatureNotInstalledException(RaptorDomainFeature)
