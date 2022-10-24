package io.fluidsonic.raptor

import kotlinx.serialization.*


public interface RaptorJobGroup<Data> {

	public val id: String
	public val serializer: KSerializer<Data>
}


@RaptorDsl
public inline fun <reified Data> RaptorJobsDsl.group(id: String): RaptorJobGroup<Data> =
	group(id = id, serializer = serializer())


@RaptorDsl
@Suppress("UnusedReceiverParameter")
public fun <Data> RaptorJobsDsl.group(id: String, serializer: KSerializer<Data>): RaptorJobGroup<Data> =
	RaptorJobGroupImpl(id = id, serializer = serializer)
