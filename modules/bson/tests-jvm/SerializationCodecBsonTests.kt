package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import kotlin.test.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.modules.*
import org.bson.*
import tests.utility.*


// The contract of `RaptorBsonSerializationCodec`, Raptor's `kotlinx.serialization`-based way of writing a BSON
// codec: does an `Encoder`/`Decoder` implemented directly against `org.bson.BsonReader`/`BsonWriter` produce
// byte-for-byte the same BSON as the equivalent hand-written Raptor codec, does its by-name field matching
// really tolerate reordered and unknown fields, and does it default to Raptor's null handling?
//
// Nothing here touches a production path: the codecs are constructed directly or resolved from throwaway
// registries built by `testRegistryWith`, and `RaptorBsonDefinition.raptorDefaults` is untouched.
class SerializationCodecBsonTests {

	private val handWrittenPrimitiveCodec = testRegistryWith(minimalPrimitiveDocumentHandWrittenDefinition)
		.get(MinimalPrimitiveDocument::class.java)

	private val handWrittenWideCodec = testRegistryWith(minimalWideDocumentHandWrittenDefinition)
		.get(MinimalWideDocument::class.java)


	// The headline comparison: the from-scratch format and the hand-written Raptor codec must produce the
	// identical wire document for the identical values.
	@Test
	fun wideDocumentBytesAreIdenticalAcrossBothCodecs() {
		assertContentEquals(
			actual = minimalWideDocumentCodec.encodeTopLevelBytes(minimalWideDocumentSample),
			expected = handWrittenWideCodec.encodeTopLevelBytes(minimalWideDocumentSample),
		)
	}


	// Pins the wire format independently of the other two codecs, so a change to all three cannot silently
	// move the stored format while the comparison above still passes.
	@Test
	fun wideDocumentMatchesAHandBuiltGoldenDocument() {
		val goldenBytes = documentBytes {
			writeName("a")
			writeInt32(42)
			writeName("b")
			writeInt64(42_000_000_000L)
			writeName("c")
			writeDouble(3.14159)
			writeName("d")
			writeBoolean(true)
			writeName("e")
			writeString("hello world")
			writeName("f")
			writeDateTime(1_705_314_600_000L)
			writeName("g")
			writeString("DE")
			writeName("h")
			writeStartArray()
			writeString("a")
			writeString("b")
			writeString("c")
			writeEndArray()
		}

		assertContentEquals(
			actual = minimalWideDocumentCodec.encodeTopLevelBytes(minimalWideDocumentSample),
			expected = goldenBytes,
		)
		assertEquals(
			actual = minimalWideDocumentCodec.decodeTopLevelValue(goldenBytes),
			expected = minimalWideDocumentSample,
		)
	}


	// The escape hatch must land on BSON DateTime, not BSON Int64 — the one place a custom serializer could
	// regress into a silently wrong stored type.
	@Test
	fun timestampFieldIsWrittenAsBsonDateTimeNotInt64() {
		val bytes = minimalWideDocumentCodec.encodeTopLevelBytes(minimalWideDocumentSample)

		assertEquals(actual = RawBsonDocument(bytes)["f"]?.bsonType, expected = BsonType.DATE_TIME)
	}


	@Test
	fun wideDocumentDecodesWhatTheHandWrittenCodecWrote() {
		val bytes = handWrittenWideCodec.encodeTopLevelBytes(minimalWideDocumentSample)

		assertEquals(actual = minimalWideDocumentCodec.decodeTopLevelValue(bytes), expected = minimalWideDocumentSample)
	}


	@Test
	fun handWrittenCodecDecodesWhatTheWideDocumentCodecWrote() {
		val bytes = minimalWideDocumentCodec.encodeTopLevelBytes(minimalWideDocumentSample)

		assertEquals(actual = handWrittenWideCodec.decodeTopLevelValue(bytes), expected = minimalWideDocumentSample)
	}


	// The one-call entry point must be exactly the two-step `RaptorBsonSerializationCodec.of(…)` +
	// `RaptorBsonDefinition.of(…)` combination it replaces — same wire bytes, same round trip.
	@Test
	fun serializableDefinitionMatchesTheTwoStepCodecAndDefinition() {
		val oneStepCodec = testRegistryWith(
			raptor.bson.serializableDefinition<MinimalWideDocument>(serializersModule = EmptySerializersModule()),
		).get(MinimalWideDocument::class.java)

		val twoStepCodec = testRegistryWith(
			RaptorBsonDefinition.of(
				RaptorBsonSerializationCodec.of<MinimalWideDocument>(serializersModule = EmptySerializersModule()),
			),
		).get(MinimalWideDocument::class.java)

		val actualBytes = oneStepCodec.encodeTopLevelBytes(minimalWideDocumentSample)

		assertContentEquals(actual = actualBytes, expected = twoStepCodec.encodeTopLevelBytes(minimalWideDocumentSample))
		assertEquals(actual = oneStepCodec.decodeTopLevelValue(actualBytes), expected = minimalWideDocumentSample)
	}


	// `preserveNulls` has to survive the trip through the convenience entry point, otherwise the one-call form
	// silently loses a setting the two-step form has.
	@Test
	fun serializableDefinitionForwardsPreserveNulls() {
		val preservingCodec = testRegistryWith(
			raptor.bson.serializableDefinition<MinimalNullableDocument>(
				serializersModule = EmptySerializersModule(),
				preserveNulls = true,
			),
		).get(MinimalNullableDocument::class.java)

		assertContentEquals(
			actual = preservingCodec.encodeTopLevelBytes(MinimalNullableDocument(value = null)),
			expected = documentBytes {
				writeName("value")
				writeNull()
			},
		)
	}


	// `RaptorBsonDefinition.of(codec)` matches on `codec.encoderClass == valueClass.java`, so wrapping only
	// works if the codec reports the concrete record class. Otherwise the registry silently falls through to
	// another definition instead of failing loudly, and the benchmark row would measure the wrong codec.
	@Test
	fun codecReportsTheConcreteRecordAsItsEncoderClass() {
		assertEquals(actual = minimalWideDocumentCodec.encoderClass, expected = MinimalWideDocument::class.java)
	}


	// Proves the registry really routes into the from-scratch codec, which is what the benchmark row measures.
	@Test
	fun registryResolvedCodecRoutesIntoTheSerializationCodec() {
		val registryCodec = testRegistryWith(minimalWideDocumentDefinition).get(MinimalWideDocument::class.java)

		assertContentEquals(
			actual = registryCodec.encodeTopLevelBytes(minimalWideDocumentSample),
			expected = minimalWideDocumentCodec.encodeTopLevelBytes(minimalWideDocumentSample),
		)
	}


	@Test
	fun primitiveDocumentMatchesAHandBuiltGoldenDocument() {
		val actualBytes = minimalPrimitiveDocumentCodec.encodeTopLevelBytes(minimalPrimitiveDocumentSample)
		val goldenBytes = documentBytes {
			writeName("a")
			writeInt32(42)
			writeName("b")
			writeInt64(42_000_000_000L)
			writeName("c")
			writeDouble(3.14159)
			writeName("d")
			writeBoolean(true)
			writeName("e")
			writeString("hello world")
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun primitiveDocumentMatchesTheHandWrittenCodecBytes() {
		assertContentEquals(
			actual = minimalPrimitiveDocumentCodec.encodeTopLevelBytes(minimalPrimitiveDocumentSample),
			expected = handWrittenPrimitiveCodec.encodeTopLevelBytes(minimalPrimitiveDocumentSample),
		)
	}


	@Test
	fun primitiveDocumentRoundTripsThroughTheHandWrittenCodec() {
		val bytes = handWrittenPrimitiveCodec.encodeTopLevelBytes(minimalPrimitiveDocumentSample)

		assertEquals(
			actual = minimalPrimitiveDocumentCodec.decodeTopLevelValue(bytes),
			expected = minimalPrimitiveDocumentSample,
		)
	}


	// The whole justification for by-name matching rather than kotlinx-serialization's
	// `decodeSequentially()` fast path: genki-core replays events written by many code versions over time,
	// so no wire order can be trusted.
	@Test
	fun documentFieldsAreMatchedByNameRegardlessOfWireOrder() {
		val reorderedBytes = documentBytes {
			writeName("e")
			writeString("hello world")
			writeName("c")
			writeDouble(3.14159)
			writeName("a")
			writeInt32(42)
			writeName("d")
			writeBoolean(true)
			writeName("b")
			writeInt64(42_000_000_000L)
		}

		assertEquals(
			actual = minimalPrimitiveDocumentCodec.decodeTopLevelValue(reorderedBytes),
			expected = minimalPrimitiveDocumentSample,
		)
	}


	// The other half of the same requirement: fields removed from the Kotlin class are still present in
	// historical documents and must be skipped rather than rejected.
	@Test
	fun unknownDocumentFieldsAreSkipped() {
		val bytesWithExtraFields = documentBytes {
			writeName("removedLongAgo")
			writeString("junk")
			writeName("a")
			writeInt32(42)
			writeName("b")
			writeInt64(42_000_000_000L)
			writeName("alsoGone")
			writeStartDocument()
			writeName("nested")
			writeInt32(7)
			writeEndDocument()
			writeName("c")
			writeDouble(3.14159)
			writeName("d")
			writeBoolean(true)
			writeName("e")
			writeString("hello world")
			writeName("trailingExtra")
			writeStartArray()
			writeInt32(1)
			writeInt32(2)
			writeEndArray()
		}

		assertEquals(
			actual = minimalPrimitiveDocumentCodec.decodeTopLevelValue(bytesWithExtraFields),
			expected = minimalPrimitiveDocumentSample,
		)
	}


	@Test
	fun absentPropertyWithAKotlinDefaultFallsBackToThatDefault() {
		val bytes = documentBytes {
			writeName("required")
			writeInt32(7)
		}

		assertEquals(
			actual = minimalDefaultingDocumentCodec.decodeTopLevelValue(bytes),
			expected = MinimalDefaultingDocument(required = 7),
		)
	}


	// `MissingFieldException` is itself `@ExperimentalSerializationApi`; asserting the concrete type is the
	// point of the test, so opting in beats weakening the assertion to `SerializationException`.
	@OptIn(ExperimentalSerializationApi::class)
	@Test
	fun absentPropertyWithoutAKotlinDefaultFails() {
		val bytes = documentBytes {
			writeName("optional")
			writeString("present")
		}

		assertFailsWith<MissingFieldException> {
			minimalDefaultingDocumentCodec.decodeTopLevelValue(bytes)
		}
	}


	// The convention this format has to share with every hand-written Raptor codec: a null property is left out
	// of the document entirely unless the caller asks for an explicit null.
	@Test
	fun nullPropertyIsOmittedByDefault() {
		val handWrittenNullableCodec = testRegistryWith(minimalNullableDocumentOmittingNullHandWrittenDefinition)
			.get(MinimalNullableDocument::class.java)
		val document = MinimalNullableDocument(value = null)

		val actualBytes = minimalNullableDocumentCodec.encodeTopLevelBytes(document)

		assertContentEquals(actual = actualBytes, expected = handWrittenNullableCodec.encodeTopLevelBytes(document))
		assertContentEquals(actual = actualBytes, expected = documentBytes {})
	}


	@Test
	fun nullPropertyIsWrittenAsAnExplicitBsonNullWhenPreservingNulls() {
		val handWrittenNullableCodec = testRegistryWith(minimalNullableDocumentHandWrittenDefinition)
			.get(MinimalNullableDocument::class.java)
		val document = MinimalNullableDocument(value = null)

		val actualBytes = minimalNullableDocumentPreservingNullCodec.encodeTopLevelBytes(document)

		assertContentEquals(actual = actualBytes, expected = handWrittenNullableCodec.encodeTopLevelBytes(document))
		assertContentEquals(
			actual = actualBytes,
			expected = documentBytes {
				writeName("value")
				writeNull()
			},
		)
	}


	@Test
	fun explicitNullPropertyRoundTrips() {
		val bytes = minimalNullableDocumentPreservingNullCodec.encodeTopLevelBytes(MinimalNullableDocument(value = null))

		assertEquals(
			actual = minimalNullableDocumentPreservingNullCodec.decodeTopLevelValue(bytes),
			expected = MinimalNullableDocument(value = null),
		)
	}


	// A codec preserving nulls must still accept a document that omits the field, and vice versa — the two
	// settings only change what is written, never what can be read.
	@Test
	fun omittedNullPropertyRoundTripsWhenTheKotlinDefaultIsNull() {
		val bytes = minimalDefaultedNullableDocumentCodec.encodeTopLevelBytes(MinimalDefaultedNullableDocument(value = null))

		assertContentEquals(actual = bytes, expected = documentBytes {})
		assertEquals(
			actual = minimalDefaultedNullableDocumentCodec.decodeTopLevelValue(bytes),
			expected = MinimalDefaultedNullableDocument(value = null),
		)
	}


	// The trade-off omitting nulls brings, and the reason `preserveNulls = true` exists: `kotlinx.serialization`
	// cannot tell an omitted field from one that was never written, so a nullable property without a Kotlin
	// default no longer round-trips.
	@OptIn(ExperimentalSerializationApi::class)
	@Test
	fun omittedNullPropertyFailsToDecodeWithoutAKotlinDefault() {
		val bytes = minimalNullableDocumentCodec.encodeTopLevelBytes(MinimalNullableDocument(value = null))

		assertFailsWith<MissingFieldException> {
			minimalNullableDocumentCodec.decodeTopLevelValue(bytes)
		}
	}


	// Omitting a null array element would shift every later element's position, so `preserveNulls` must not
	// reach inside a BSON array.
	@Test
	fun nullArrayElementsAreAlwaysWrittenAsExplicitBsonNulls() {
		val document = MinimalNullableElementListDocument(values = listOf("a", null, "c"))

		val bytes = minimalNullableElementListDocumentCodec.encodeTopLevelBytes(document)

		assertContentEquals(
			actual = bytes,
			expected = documentBytes {
				writeName("values")
				writeStartArray()
				writeString("a")
				writeNull()
				writeString("c")
				writeEndArray()
			},
		)
		assertEquals(actual = minimalNullableElementListDocumentCodec.decodeTopLevelValue(bytes), expected = document)
	}


	@Test
	fun nestedDocumentAndArraysMatchAHandBuiltGoldenDocument() {
		val goldenBytes = documentBytes {
			writeName("name")
			writeString("outer")
			writeName("tags")
			writeStartArray()
			writeString("a")
			writeString("b")
			writeString("c")
			writeEndArray()
			writeName("rows")
			writeStartArray()
			writeStartArray()
			writeInt32(1)
			writeInt32(2)
			writeEndArray()
			writeStartArray()
			writeEndArray()
			writeStartArray()
			writeInt32(3)
			writeEndArray()
			writeEndArray()
			writeName("inner")
			writeStartDocument()
			writeName("a")
			writeInt32(42)
			writeName("b")
			writeInt64(42_000_000_000L)
			writeName("c")
			writeDouble(3.14159)
			writeName("d")
			writeBoolean(true)
			writeName("e")
			writeString("hello world")
			writeEndDocument()
		}

		assertContentEquals(
			actual = minimalNestedDocumentCodec.encodeTopLevelBytes(minimalNestedDocumentSample),
			expected = goldenBytes,
		)
		assertEquals(
			actual = minimalNestedDocumentCodec.decodeTopLevelValue(goldenBytes),
			expected = minimalNestedDocumentSample,
		)
	}


	// `of` must resolve the root serializer through the caller's `serializersModule`, not through the
	// format-agnostic top-level `serializer<Value>()` — otherwise a root type that relies on a contextual
	// serializer registered in that module (rather than a compile-time `@Serializable` declaration) can never
	// be encoded at all.
	@Test
	fun ofResolvesARootSerializerThroughTheSerializersModule() {
		val codec = RaptorBsonSerializationCodec.of<ContextualRootProbe>(serializersModule = contextualRootProbeModule)

		val bytes = codec.encodeTopLevelBytes(ContextualRootProbe(value = "probe"))

		assertEquals(actual = codec.decodeTopLevelValue(bytes), expected = ContextualRootProbe(value = "probe"))
	}


	@Test
	fun presentNullablePropertyRoundTrips() {
		val bytes = minimalNullableDocumentCodec.encodeTopLevelBytes(MinimalNullableDocument(value = "present"))

		assertEquals(
			actual = minimalNullableDocumentCodec.decodeTopLevelValue(bytes),
			expected = MinimalNullableDocument(value = "present"),
		)
	}
}
