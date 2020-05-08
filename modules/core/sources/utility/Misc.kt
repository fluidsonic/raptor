package io.fluidsonic.raptor


// FIXME move to fluid-stdlib?
fun stackTrace(skipCount: Int = 0) =
	Thread.currentThread().stackTrace.drop(skipCount + 2) // +1 to exclude this, +1 to exclude Thread.getStackTrace()
