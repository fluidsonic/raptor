package tests

import io.fluidsonic.country.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import io.fluidsonic.time.*
import kotlin.test.*
import org.bson.*
import tests.utility.*


// Shared `RaptorBsonType` instances used for both encode and decode below, proving the write path reuses the
// exact same lazy, registry-keyed codec binding ([RaptorBsonType.codec]) that the read path already relies on.
private val writeCountryType = raptor.bson.type<Country>()
private val writeTimestampType = raptor.bson.type<Timestamp>()
private val writeStringListType = raptor.bson.type<List<String>>()
private val writeCountrySetType = raptor.bson.type<Set<Country>>()
private val writeIntType = raptor.bson.type<Int>()
private val writeLongType = raptor.bson.type<Long>()
private val writeDoubleType = raptor.bson.type<Double>()
private val writeBooleanType = raptor.bson.type<Boolean>()


private data class WriteTypedScalarHolder(val country: Country, val timestamp: Timestamp)


private fun writeTypedScalarHolderDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<WriteTypedScalarHolder> {
		decode {
			reader.document {
				WriteTypedScalarHolder(
					country = valueOrThrow("country", writeCountryType),
					timestamp = valueOrThrow("timestamp", writeTimestampType),
				)
			}
		}

		encode { holder ->
			writer.document {
				value("country", writeCountryType, holder.country)
				value("timestamp", writeTimestampType, holder.timestamp)
			}
		}
	}


private data class WriteTypedCollectionHolder(val strings: List<String>, val countries: Set<Country>)


private fun writeTypedCollectionHolderDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<WriteTypedCollectionHolder> {
		decode {
			reader.document {
				WriteTypedCollectionHolder(
					strings = valueOrThrow("strings", writeStringListType),
					countries = valueOrThrow("countries", writeCountrySetType),
				)
			}
		}

		encode { holder ->
			writer.document {
				value("strings", writeStringListType, holder.strings)
				value("countries", writeCountrySetType, holder.countries)
			}
		}
	}


private data class WriteTypedNullableHolder(val country: Country?)


private fun writeTypedNullableHolderDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<WriteTypedNullableHolder> {
		decode {
			reader.document {
				WriteTypedNullableHolder(country = valueOrNull("country", writeCountryType))
			}
		}

		encode { holder ->
			writer.document {
				value("country", writeCountryType, holder.country, preserveNull = true)
			}
		}
	}


private data class WriteTypedOmitNullHolder(val country: Country?)


private fun writeTypedOmitNullHolderDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<WriteTypedOmitNullHolder> {
		decode {
			reader.document {
				WriteTypedOmitNullHolder(country = valueOrNull("country", writeCountryType))
			}
		}

		encode { holder ->
			writer.document {
				value("country", writeCountryType, holder.country)
			}
		}
	}


private data class WriteTypedPrimitiveHolder(
	val intValue: Int,
	val longValue: Long,
	val doubleValue: Double,
	val booleanValue: Boolean,
)


private fun writeTypedPrimitiveHolderDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<WriteTypedPrimitiveHolder> {
		decode {
			reader.document {
				WriteTypedPrimitiveHolder(
					intValue = valueOrThrow("intValue", writeIntType),
					longValue = valueOrThrow("longValue", writeLongType),
					doubleValue = valueOrThrow("doubleValue", writeDoubleType),
					booleanValue = valueOrThrow("booleanValue", writeBooleanType),
				)
			}
		}

		encode { holder ->
			writer.document {
				value("intValue", writeIntType, holder.intValue)
				value("longValue", writeLongType, holder.longValue)
				value("doubleValue", writeDoubleType, holder.doubleValue)
				value("booleanValue", writeBooleanType, holder.booleanValue)
			}
		}
	}


// Write-side counterpart to `RaptorBsonTypeTests.rebindsToTheCorrectCodecAcrossSeparateRegistries` —
// proves the write path also re-binds to the querying registry's codec rather than a stale one.
private data class WriteRebindProbe(val tag: String)


private fun writeRebindProbeDefinition(suffix: String): RaptorBsonDefinition =
	raptor.bson.definition<WriteRebindProbe> {
		decode {
			reader.document {
				WriteRebindProbe(tag = string("tag"))
			}
		}

		encode { probe ->
			writer.document {
				value("tag", probe.tag + suffix)
			}
		}
	}


private val writeRebindProbeType = raptor.bson.type<WriteRebindProbe>()


private data class WriteRebindProbeHolder(val probe: WriteRebindProbe)


private fun writeRebindProbeHolderDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<WriteRebindProbeHolder> {
		decode {
			reader.document {
				WriteRebindProbeHolder(probe = valueOrThrow("probe", writeRebindProbeType))
			}
		}

		encode { holder ->
			writer.document {
				value("probe", writeRebindProbeType, holder.probe)
			}
		}
	}


class RaptorBsonTypeWriteTests {

	@Test
	fun encodesCodecBackedScalarsThroughTypes() {
		val registry = testRegistryWith(writeTypedScalarHolderDefinition())
		val codec = registry.get(WriteTypedScalarHolder::class.java)
		val holder = WriteTypedScalarHolder(
			country = Country.forCode(CountryCode.parse("DE")),
			timestamp = Timestamp.fromEpochMilliseconds(1_705_314_600_000L),
		)

		val actualBytes = codec.encodeTopLevelBytes(holder)
		val goldenBytes = documentBytes {
			writeName("country")
			writeString("DE")
			writeName("timestamp")
			writeDateTime(1_705_314_600_000L)
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
		assertEquals(actual = codec.decodeTopLevelValue(goldenBytes), expected = holder)
	}


	@Test
	fun encodesTypedListAndSetThroughTypes() {
		val registry = testRegistryWith(writeTypedCollectionHolderDefinition())
		val codec = registry.get(WriteTypedCollectionHolder::class.java)
		val holder = WriteTypedCollectionHolder(
			strings = listOf("a", "b", "c"),
			countries = setOf(Country.forCode(CountryCode.parse("DE")), Country.forCode(CountryCode.parse("US"))),
		)

		val actualBytes = codec.encodeTopLevelBytes(holder)
		val goldenBytes = documentBytes {
			writeName("strings")
			writeStartArray()
			writeString("a")
			writeString("b")
			writeString("c")
			writeEndArray()
			writeName("countries")
			writeStartArray()
			writeString("DE")
			writeString("US")
			writeEndArray()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
		assertEquals(actual = codec.decodeTopLevelValue(goldenBytes), expected = holder)
	}


	// Writing null goes through the plain non-null `writeCountryType`: the write overload takes a `Value?` and
	// discriminates on the value itself plus `preserveNull`, so no nullable `RaptorBsonType` is involved. This
	// test pins the `preserveNull = true` half; `omitsNullFieldWhenPreserveNullIsFalseThroughType` the other.
	@Test
	fun encodesExplicitNullWhenPreserveNullIsTrueThroughType() {
		val registry = testRegistryWith(writeTypedNullableHolderDefinition())
		val codec = registry.get(WriteTypedNullableHolder::class.java)

		val actualBytes = codec.encodeTopLevelBytes(WriteTypedNullableHolder(country = null))
		val goldenBytes = documentBytes {
			writeName("country")
			writeNull()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
		assertEquals(actual = codec.decodeTopLevelValue(goldenBytes), expected = WriteTypedNullableHolder(country = null))
	}


	@Test
	fun omitsNullFieldWhenPreserveNullIsFalseThroughType() {
		val registry = testRegistryWith(writeTypedOmitNullHolderDefinition())
		val codec = registry.get(WriteTypedOmitNullHolder::class.java)

		val actualBytes = codec.encodeTopLevelBytes(WriteTypedOmitNullHolder(country = null))
		val goldenBytes = documentBytes { }

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun encodesNonNullValueThroughNullableType() {
		val registry = testRegistryWith(writeTypedNullableHolderDefinition())
		val codec = registry.get(WriteTypedNullableHolder::class.java)

		val holder = WriteTypedNullableHolder(country = Country.forCode(CountryCode.parse("FR")))
		val actualBytes = codec.encodeTopLevelBytes(holder)
		val goldenBytes = documentBytes {
			writeName("country")
			writeString("FR")
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
	}


	@Test
	fun encodesPrimitivesThroughTypes() {
		val registry = testRegistryWith(writeTypedPrimitiveHolderDefinition())
		val codec = registry.get(WriteTypedPrimitiveHolder::class.java)
		val holder = WriteTypedPrimitiveHolder(
			intValue = 42,
			longValue = 42_000_000_000L,
			doubleValue = 3.14159,
			booleanValue = true,
		)

		val actualBytes = codec.encodeTopLevelBytes(holder)
		val goldenBytes = documentBytes {
			writeName("intValue")
			writeInt32(42)
			writeName("longValue")
			writeInt64(42_000_000_000L)
			writeName("doubleValue")
			writeDouble(3.14159)
			writeName("booleanValue")
			writeBoolean(true)
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
		assertEquals(actual = codec.decodeTopLevelValue(goldenBytes), expected = holder)
	}


	// Proves a single `RaptorBsonType` instance can decode a value and then re-encode it, exactly as a plugin
	// definition would when it declares one `raptor.bson.type<T>()` per field and uses it on both sides.
	@Test
	fun roundTripsThroughTheSameTypeInstanceForReadAndWrite() {
		val registry = testRegistryWith(writeTypedScalarHolderDefinition())
		val codec = registry.get(WriteTypedScalarHolder::class.java)
		val holder = WriteTypedScalarHolder(
			country = Country.forCode(CountryCode.parse("DE")),
			timestamp = Timestamp.fromEpochMilliseconds(1_705_314_600_000L),
		)

		val bytes = codec.encodeTopLevelBytes(holder)
		val decoded = codec.decodeTopLevelValue(bytes)
		val reEncodedBytes = codec.encodeTopLevelBytes(decoded)

		assertEquals(actual = decoded, expected = holder)
		assertContentEquals(actual = reEncodedBytes, expected = bytes)
	}


	@Test
	fun rebindsToTheCorrectCodecAcrossSeparateRegistriesForWriting() {
		val registryA = testRegistryWith(writeRebindProbeDefinition(suffix = "-A"), writeRebindProbeHolderDefinition())
		val registryB = testRegistryWith(writeRebindProbeDefinition(suffix = "-B"), writeRebindProbeHolderDefinition())

		val holder = WriteRebindProbeHolder(probe = WriteRebindProbe(tag = "probe"))

		val goldenA = documentBytes {
			writeName("probe")
			writeStartDocument()
			writeName("tag")
			writeString("probe-A")
			writeEndDocument()
		}
		val goldenB = documentBytes {
			writeName("probe")
			writeStartDocument()
			writeName("tag")
			writeString("probe-B")
			writeEndDocument()
		}

		assertContentEquals(actual = registryA.get(WriteRebindProbeHolder::class.java).encodeTopLevelBytes(holder), expected = goldenA)
		assertContentEquals(actual = registryB.get(WriteRebindProbeHolder::class.java).encodeTopLevelBytes(holder), expected = goldenB)

		// Query A again after B to prove it rebinds back rather than sticking to B's codec.
		assertContentEquals(actual = registryA.get(WriteRebindProbeHolder::class.java).encodeTopLevelBytes(holder), expected = goldenA)
	}
}
