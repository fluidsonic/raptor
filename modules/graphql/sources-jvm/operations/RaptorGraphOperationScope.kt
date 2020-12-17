package io.fluidsonic.raptor


@RaptorDsl
class RaptorGraphOperationScope internal constructor(
	delegate: RaptorGraphScope
) : RaptorGraphScope by delegate
