package io.fluidsonic.raptor.cqrs

import io.fluidsonic.raptor.*


public val RaptorContext.domain: RaptorDomain
	get() = properties[Keys.domainProperty] ?: throw RaptorFeatureNotInstalledException(RaptorDomainFeature)
