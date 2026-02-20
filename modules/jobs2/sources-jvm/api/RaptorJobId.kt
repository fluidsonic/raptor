package io.fluidsonic.raptor.jobs2

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.domain.*


@JvmName("jobIdUnit")
@RaptorDsl
public fun RaptorGlobalDsl.jobId(name: String): RaptorJobId<Unit, Unit> =
	RaptorJobId(name)


@JvmName("jobIdInput")
@RaptorDsl
public fun <Input> RaptorGlobalDsl.jobId(name: String): RaptorJobId<Input, Unit> =
	RaptorJobId(name)


@JvmName("jobIdInputOutput")
@RaptorDsl
public fun <Input, Output> RaptorGlobalDsl.jobId(name: String): RaptorJobId<Input, Output> =
	RaptorJobId(name)


@JvmInline
public value class RaptorJobId<Input, Output>(private val value: String) : RaptorAggregateId {

	override val discriminator: String
		get() = "RaptorJobId"

	override fun toString(): String =
		value
}
