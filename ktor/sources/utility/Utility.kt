package io.fluidsonic.raptor


internal fun <Receiver> Iterable<Receiver.() -> Unit>.flatten(): Receiver.() -> Unit = {
	for (element in this@flatten)
		element()
}
