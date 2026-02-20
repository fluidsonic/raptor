package io.fluidsonic.raptor.jobs2

import io.fluidsonic.raptor.domain.*
import kotlinx.serialization.*


/**
 * Declarative description of a job type, including serializers for persistence.
 *
 * Each description has a unique [id] that identifies the job type.
 * The [inputSerializer] and [outputSerializer] are used for durable storage and must produce stable formats across versions.
 */
public interface RaptorJobDescription<Input, Output> {

	public val id: JobDescriptionId<Input, Output>
	public val inputSerializer: KSerializer<Input>
	public val outputSerializer: KSerializer<Output>
	// FIXME timeout?
	// FIXME batch size? // FIXME in handler?
	// FIXME maximum concurrency? // FIXME in handler?
	// FIXME unique input?
	// FIXME retry
}


/**
 * Identifier for a job description type.
 */
@JvmInline
public value class JobDescriptionId<Input, Output>(private val value: String) : RaptorEntityId {

	override fun toString(): String =
		value
}
