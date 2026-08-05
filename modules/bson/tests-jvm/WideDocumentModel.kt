package tests

import io.fluidsonic.country.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import io.fluidsonic.time.*


// TEST FIXTURE — not wired into any production path. A flat eight-field record shape, the counterpart to the
// deeply nested `ComplexOrder` shape in `ComplexNestedModel.kt`: scalar fields of several different Kotlin
// types plus one generic collection field. The same shape is defined twice below — once read and written
// through the reified `reader.value<T>()` / `writer.value(field, value)` paths and once through
// `raptor.bson.type<T>()` tokens — so `PositionIndependentDecodingTests` covers both per-field dispatch paths.
//
// Every decode block below dispatches by field name through `reader.documentByField { }`, never as a
// positional `reader.document { }` sequence, because that is what genki-core's codecs actually have to do:
// they replay events written by many code versions over time, so no wire order can ever be trusted (see
// `RaptorAggregateEventBson`, and the idiom in `extensions/GeoCoordinate.kt`). The decode blocks therefore
// carry the by-name `when` dispatch, the nullable locals and the per-property `missingFieldValue` check that
// the real codecs carry, and `PositionIndependentDecodingTests` is what holds that property in place.
//
// The `encode` blocks stay positional on purpose: an encoder choosing its own fixed field order is normal
// and realistic — only a decoder assuming that order is not.


// A representative "genki-core event" shape, with each field read through the reified `reader.value<T>()`
// path — the same per-field dispatch (`DefaultBsonReaderScope.value(type)`) that a production event with
// dozens of typed fields exercises once per field on every replayed event.
internal data class WideDocument(
	val a: Int,
	val b: Long,
	val c: Double,
	val d: Boolean,
	val e: String,
	val f: Timestamp,
	val g: Country,
	val h: List<String>,
)


internal val wideDocumentDefinition: RaptorBsonDefinition =
	raptor.bson.definition<WideDocument> {
		decode {
			var a: Int? = null
			var b: Long? = null
			var c: Double? = null
			var d: Boolean? = null
			var e: String? = null
			var f: Timestamp? = null
			var g: Country? = null
			var h: List<String>? = null

			reader.documentByField { field ->
				when (field) {
					"a" -> a = value<Int>()
					"b" -> b = value<Long>()
					"c" -> c = value<Double>()
					"d" -> d = value<Boolean>()
					"e" -> e = value<String>()
					"f" -> f = value<Timestamp>()
					"g" -> g = value<Country>()
					"h" -> h = value<List<String>>()
					else -> skipValue()
				}
			}

			WideDocument(
				a = a ?: missingFieldValue("a"),
				b = b ?: missingFieldValue("b"),
				c = c ?: missingFieldValue("c"),
				d = d ?: missingFieldValue("d"),
				e = e ?: missingFieldValue("e"),
				f = f ?: missingFieldValue("f"),
				g = g ?: missingFieldValue("g"),
				h = h ?: missingFieldValue("h"),
			)
		}

		encode { doc ->
			writer.document {
				value("a", doc.a)
				value("b", doc.b)
				value("c", doc.c)
				value("d", doc.d)
				value("e", doc.e)
				value("f", doc.f)
				value("g", doc.g)
				value("h", doc.h)
			}
		}
	}


// The value every scrambled document in `PositionIndependentDecodingTests` must decode to, and the value
// [typedWideDocumentSample] mirrors field for field.
internal val wideDocumentSample = WideDocument(
	a = 42,
	b = 42_000_000_000L,
	c = 3.14159,
	d = true,
	e = "hello world",
	f = Timestamp.fromEpochMilliseconds(1_705_314_600_000L),
	g = Country.forCode(CountryCode.parse("DE")),
	h = listOf("a", "b", "c"),
)


// The same shape again, but with every field read AND written through a `raptor.bson.type<T>()` token
// resolved once here at class-init instead of through the reified `reader.value<T>(field)` /
// `writer.value(field, value)` paths: `Int`/`Long`/`Double`/`Boolean` hit the primitive-specialized overloads,
// `String`/`Timestamp`/`Country` reuse the token's cached codec binding, and `List<String>` reuses the token's
// precomputed element type. Position-independent decoding has to hold on this path too, which is why the shape
// is duplicated rather than shared.
private val intType = raptor.bson.type<Int>()
private val longType = raptor.bson.type<Long>()
private val doubleType = raptor.bson.type<Double>()
private val booleanType = raptor.bson.type<Boolean>()
private val stringType = raptor.bson.type<String>()
private val timestampType = raptor.bson.type<Timestamp>()
private val countryType = raptor.bson.type<Country>()
private val stringListType = raptor.bson.type<List<String>>()


internal data class TypedWideDocument(
	val a: Int,
	val b: Long,
	val c: Double,
	val d: Boolean,
	val e: String,
	val f: Timestamp,
	val g: Country,
	val h: List<String>,
)


internal val typedWideDocumentDefinition: RaptorBsonDefinition =
	raptor.bson.definition<TypedWideDocument> {
		decode {
			var a: Int? = null
			var b: Long? = null
			var c: Double? = null
			var d: Boolean? = null
			var e: String? = null
			var f: Timestamp? = null
			var g: Country? = null
			var h: List<String>? = null

			reader.documentByField { field ->
				when (field) {
					"a" -> a = valueOrThrow(intType)
					"b" -> b = valueOrThrow(longType)
					"c" -> c = valueOrThrow(doubleType)
					"d" -> d = valueOrThrow(booleanType)
					"e" -> e = valueOrThrow(stringType)
					"f" -> f = valueOrThrow(timestampType)
					"g" -> g = valueOrThrow(countryType)
					"h" -> h = valueOrThrow(stringListType)
					else -> skipValue()
				}
			}

			TypedWideDocument(
				a = a ?: missingFieldValue("a"),
				b = b ?: missingFieldValue("b"),
				c = c ?: missingFieldValue("c"),
				d = d ?: missingFieldValue("d"),
				e = e ?: missingFieldValue("e"),
				f = f ?: missingFieldValue("f"),
				g = g ?: missingFieldValue("g"),
				h = h ?: missingFieldValue("h"),
			)
		}

		encode { doc ->
			writer.document {
				value("a", intType, doc.a)
				value("b", longType, doc.b)
				value("c", doubleType, doc.c)
				value("d", booleanType, doc.d)
				value("e", stringType, doc.e)
				value("f", timestampType, doc.f)
				value("g", countryType, doc.g)
				value("h", stringListType, doc.h)
			}
		}
	}


internal val typedWideDocumentSample = TypedWideDocument(
	a = 42,
	b = 42_000_000_000L,
	c = 3.14159,
	d = true,
	e = "hello world",
	f = Timestamp.fromEpochMilliseconds(1_705_314_600_000L),
	g = Country.forCode(CountryCode.parse("DE")),
	h = listOf("a", "b", "c"),
)
