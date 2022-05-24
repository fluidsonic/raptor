package io.fluidsonic.raptor

import kotlinx.coroutines.*


@RaptorDsl
public interface RaptorLifecycleScope : RaptorScope, CoroutineScope
