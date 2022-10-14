package io.fluidsonic.raptor.lifecycle

import io.fluidsonic.raptor.*
import kotlinx.coroutines.*


@RaptorDsl
public interface RaptorLifecycleScope : RaptorScope, CoroutineScope
