// FIXME Duration doesn't support ISO 8601 duration format & DateTimePeriod does only serialization
//package io.fluidsonic.raptor
//
//import io.fluidsonic.time.*
//import kotlin.time.*
//import kotlin.time.Duration.*
//
//
//public fun Companion.bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<Duration> {
//	decode<String> { parse(it) ?: error("Invalid ISO 8601 duration format: $it") } // FIXME move non-null parsing to fluid-time
//	encode(Duration::toString)
//}
