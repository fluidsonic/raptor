package io.fluidsonic.raptor

import kotlinx.serialization.*


internal class RaptorJobGroupImpl<Data>(
	override val id: String,
	override val serializer: KSerializer<Data>,
) : RaptorJobGroup<Data>
