package io.fluidsonic.raptor


internal fun <T> identity(value: T) =
	value


internal fun stackTrace(skipCount: Int = 0) =
	Thread.currentThread().stackTrace.drop(skipCount + 2) // +1 to exclude this, +1 to exclude Thread.getStackTrace()


internal fun <Receiver> Iterable<Receiver.() -> Unit>.flatten(): Receiver.() -> Unit = {
	for (element in this@flatten)
		element()
}


internal fun <Result> Iterable<() -> Result>.invokeAll() =
	map { it() }
