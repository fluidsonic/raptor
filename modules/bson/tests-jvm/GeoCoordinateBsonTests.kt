package tests

import io.fluidsonic.stdlib.*
import kotlin.reflect.*
import kotlin.test.*
import org.bson.*
import tests.utility.*


// `GeoCoordinate` is the only document-shaped value type among the Raptor defaults: it encodes as a
// GeoJSON Point — `{ coordinates: [longitude, latitude], type: "Point" }` — with longitude *first*.
// Because the codec owns the entire top-level BSON value, these tests use `encodeTopLevelBytes` /
// `decodeTopLevelValue` rather than the synthetic `{ value: … }` field wrapper.
class GeoCoordinateBsonTests {

	private val berlin = GeoCoordinate(latitude = 52.5, longitude = 13.4)
	private val paris = GeoCoordinate(latitude = 48.9, longitude = 2.3)


	private fun goldenPointBytes(longitude: Double, latitude: Double): ByteArray =
		documentBytes {
			writeName("coordinates")
			writeStartArray()
			writeDouble(longitude)
			writeDouble(latitude)
			writeEndArray()
			writeName("type")
			writeString("Point")
		}


	@Test
	fun encodesGeoCoordinateAsGeoJsonPointDocument() {
		val codec = codecFor(GeoCoordinate::class)

		val actualBytes = codec.encodeTopLevelBytes(berlin)
		val goldenBytes = goldenPointBytes(longitude = 13.4, latitude = 52.5)

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun decodesGeoCoordinateFromGeoJsonPointDocument() {
		val codec = codecFor(GeoCoordinate::class)

		val bytes = goldenPointBytes(longitude = 13.4, latitude = 52.5)

		assertEquals(actual = codec.decodeTopLevelValue(bytes), expected = berlin)
	}


	// The decode block dispatches on field name with `else -> skipValue()`, so an unexpected field
	// must neither fail nor desynchronize the reader.
	@Test
	fun skipsUnknownFieldsWhenDecodingGeoCoordinate() {
		val codec = codecFor(GeoCoordinate::class)

		val bytes = documentBytes {
			writeName("coordinates")
			writeStartArray()
			writeDouble(13.4)
			writeDouble(52.5)
			writeEndArray()
			writeName("type")
			writeString("Point")
			writeName("extra")
			writeString("ignored")
		}

		assertEquals(actual = codec.decodeTopLevelValue(bytes), expected = berlin)
	}


	@Test
	fun rejectsGeoCoordinateWithNonPointType() {
		val codec = codecFor(GeoCoordinate::class)

		val bytes = documentBytes {
			writeName("coordinates")
			writeStartArray()
			writeDouble(13.4)
			writeDouble(52.5)
			writeEndArray()
			writeName("type")
			writeString("NotPoint")
		}

		assertFails {
			codec.decodeTopLevelValue(bytes)
		}
	}


	@Test
	fun rejectsGeoCoordinateWithoutCoordinatesField() {
		val codec = codecFor(GeoCoordinate::class)

		val bytes = documentBytes {
			writeName("type")
			writeString("Point")
		}

		assertFails {
			codec.decodeTopLevelValue(bytes)
		}
	}


	@Test
	fun encodesAndDecodesGeoCoordinateAsTypedListElement() {
		val listCodec = codecFor(List::class)
		val coordinates = listOf(berlin, paris)

		val actualBytes = listCodec.encodeFieldBytes("value", coordinates)
		val goldenBytes = documentBytes {
			writeName("value")
			writeStartArray()
			writeStartDocument()
			writeName("coordinates")
			writeStartArray()
			writeDouble(13.4)
			writeDouble(52.5)
			writeEndArray()
			writeName("type")
			writeString("Point")
			writeEndDocument()
			writeStartDocument()
			writeName("coordinates")
			writeStartArray()
			writeDouble(2.3)
			writeDouble(48.9)
			writeEndArray()
			writeName("type")
			writeString("Point")
			writeEndDocument()
			writeEndArray()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)

		val decoded = listCodec.decodeFieldValue(
			bytes = actualBytes,
			fieldName = "value",
			arguments = listOf(KTypeProjection.covariant(typeOf<GeoCoordinate>())),
		)

		assertEquals(actual = decoded, expected = coordinates)
	}
}
