package tests

import io.fluidsonic.raptor.bson.*
import kotlin.test.*
import kotlinx.serialization.*
import kotlinx.serialization.modules.*
import org.bson.codecs.Codec
import tests.utility.*


// Does `RaptorBsonSerializationCodec` support `@JvmInline value class` properties at all, and if so does it
// write the wrapped scalar rather than a wrapper document? Raptor's hand-written definitions for inline value
// classes — the `CustomerId`/`Cents` shapes in `ComplexNestedModel.kt`, following the `kotlin.time.Duration`
// precedent in `extensions/Duration.kt` — write the bare scalar, so the two codec styles are only
// wire-compatible if the serialization format does the same.
@Serializable
@JvmInline
internal value class ProbeStringId(val value: String)


@Serializable
@JvmInline
internal value class ProbeLongAmount(val value: Long)


@Serializable
internal data class ProbeValueClassDocument(
	val id: ProbeStringId,
	val amount: ProbeLongAmount,
)


class ValueClassSerializationTests {

	private val codec: Codec<ProbeValueClassDocument> =
		RaptorBsonSerializationCodec.of(serializersModule = EmptySerializersModule())

	private val sample = ProbeValueClassDocument(
		id = ProbeStringId("probe-4711"),
		amount = ProbeLongAmount(9_999L),
	)


	// A wrapper document here instead of a bare scalar would make the format wire-incompatible with every
	// value class Raptor has already written, so this asserts the raw bytes rather than a round-tripped value.
	@Test
	fun valueClassPropertiesAreWrittenAsTheirWrappedScalar() {
		assertContentEquals(
			actual = codec.encodeTopLevelBytes(sample),
			expected = documentBytes {
				writeName("id")
				writeString("probe-4711")
				writeName("amount")
				writeInt64(9_999L)
			},
		)
	}


	@Test
	fun valueClassPropertiesRoundTrip() {
		assertEquals(actual = codec.decodeTopLevelValue(codec.encodeTopLevelBytes(sample)), expected = sample)
	}
}
