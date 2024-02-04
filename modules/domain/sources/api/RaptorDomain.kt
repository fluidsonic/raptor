package io.fluidsonic.raptor.domain

import kotlinx.coroutines.*


public interface RaptorDomain {

	public val loaded: Deferred<RaptorDomain>
}
