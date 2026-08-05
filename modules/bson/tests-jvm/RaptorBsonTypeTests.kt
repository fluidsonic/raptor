package tests

import io.fluidsonic.country.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import io.fluidsonic.time.*
import kotlin.test.*
import org.bson.*
import tests.utility.*


// `raptor.bson.type<T>()` is designed to be resolved once, outside any per-decode closure — a file-level
// `private val` is the intended usage, mirroring how `ListExtensions` keeps its `BsonTypeClassMap`.
private val countryType = raptor.bson.type<Country>()
private val timestampType = raptor.bson.type<Timestamp>()
private val stringListType = raptor.bson.type<List<String>>()
private val countrySetType = raptor.bson.type<Set<Country>>()
private val intType = raptor.bson.type<Int>()
private val longType = raptor.bson.type<Long>()
private val doubleType = raptor.bson.type<Double>()
private val booleanType = raptor.bson.type<Boolean>()


private data class TypedScalarHolder(val country: Country, val timestamp: Timestamp)


private fun typedScalarHolderDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<TypedScalarHolder> {
		decode {
			reader.document {
				TypedScalarHolder(
					country = valueOrThrow("country", countryType),
					timestamp = valueOrThrow("timestamp", timestampType),
				)
			}
		}

		encode { holder ->
			writer.document {
				value("country", holder.country)
				value("timestamp", holder.timestamp)
			}
		}
	}


// `Set` has no codec in the registry at all — it is special-cased by the reader — so a `RaptorBsonType` for
// it proves that path keeps its own collection dispatch rather than falling through to a codec lookup.
private data class TypedCollectionHolder(val strings: List<String>, val countries: Set<Country>)


private fun typedCollectionHolderDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<TypedCollectionHolder> {
		decode {
			reader.document {
				TypedCollectionHolder(
					strings = valueOrThrow("strings", stringListType),
					countries = valueOrThrow("countries", countrySetType),
				)
			}
		}

		encode { holder ->
			writer.document {
				value("strings", holder.strings)

				array("countries") {
					for (country in holder.countries)
						value(country)
				}
			}
		}
	}


private data class TypedNullableHolder(val country: Country?)


private fun typedNullableHolderDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<TypedNullableHolder> {
		decode {
			reader.document {
				TypedNullableHolder(country = valueOrNull("country", countryType))
			}
		}

		encode { holder ->
			writer.document {
				value("country", holder.country, preserveNull = true)
			}
		}
	}


// A collection's *element* type can still be nullable, e.g. `List<Country?>` — but the element `RaptorBsonType`
// itself stays non-null (`RaptorBsonType<Value : Any>` rules out constructing a nullable-typed one at all). The
// outer one instead tracks `elementIsNullable` as a plain flag, and `collectionValue` reads that to decide
// `valueOrNull`/`valueOrThrow` per element. Top-level fields choose nullability at the call site the same way.
private val nullableCountryListType = raptor.bson.type<List<Country?>>()


private data class TypedNullableElementHolder(val countries: List<Country?>)


private fun typedNullableElementHolderDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<TypedNullableElementHolder> {
		decode {
			reader.document {
				TypedNullableElementHolder(countries = valueOrThrow("countries", nullableCountryListType))
			}
		}

		encode { holder ->
			writer.document {
				value("countries", nullableCountryListType, holder.countries)
			}
		}
	}


private data class TypedPrimitiveHolder(
	val intValue: Int,
	val longValue: Long,
	val doubleValue: Double,
	val booleanValue: Boolean,
)


private fun typedPrimitiveHolderDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<TypedPrimitiveHolder> {
		decode {
			reader.document {
				TypedPrimitiveHolder(
					intValue = valueOrThrow("intValue", intType),
					longValue = valueOrThrow("longValue", longType),
					doubleValue = valueOrThrow("doubleValue", doubleType),
					booleanValue = valueOrThrow("booleanValue", booleanType),
				)
			}
		}

		encode { holder ->
			writer.document {
				value("intValue", holder.intValue)
				value("longValue", holder.longValue)
				value("doubleValue", holder.doubleValue)
				value("booleanValue", holder.booleanValue)
			}
		}
	}


// Two holders of the same shape reading the same bytes, one through a `RaptorBsonType<Double>` and one
// through the generic reified path, so a test can pin the one place their behavior differs.
private data class TypedDoubleHolder(val value: Double)


private fun typedDoubleHolderDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<TypedDoubleHolder> {
		decode {
			reader.document {
				TypedDoubleHolder(value = valueOrThrow("value", doubleType))
			}
		}

		encode { holder ->
			writer.document {
				value("value", holder.value)
			}
		}
	}


private data class GenericDoubleHolder(val value: Double)


private fun genericDoubleHolderDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<GenericDoubleHolder> {
		decode {
			reader.document {
				GenericDoubleHolder(value = value<Double>("value"))
			}
		}

		encode { holder ->
			writer.document {
				value("value", holder.value)
			}
		}
	}


// A `RaptorBsonType` whose bound codec differs between two registries — proves `RaptorBsonType.codec(registry)`
// re-binds to whichever registry it's queried against, rather than serving a codec cached from an
// earlier registry that happens to produce the same output.
private data class RebindProbe(val tag: String)


private fun rebindProbeDefinition(suffix: String): RaptorBsonDefinition =
	raptor.bson.definition<RebindProbe> {
		decode {
			reader.document {
				RebindProbe(tag = string("tag") + suffix)
			}
		}

		encode { probe ->
			writer.document {
				value("tag", probe.tag)
			}
		}
	}


private val rebindProbeType = raptor.bson.type<RebindProbe>()


private data class RebindProbeHolder(val probe: RebindProbe)


private fun rebindProbeHolderDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<RebindProbeHolder> {
		decode {
			reader.document {
				RebindProbeHolder(probe = valueOrThrow("probe", rebindProbeType))
			}
		}

		encode { holder ->
			writer.document {
				value("probe", holder.probe)
			}
		}
	}


class RaptorBsonTypeTests {

	@Test
	fun encodesAndDecodesCodecBackedScalarsThroughTypes() {
		val registry = testRegistryWith(typedScalarHolderDefinition())
		val codec = registry.get(TypedScalarHolder::class.java)
		val holder = TypedScalarHolder(
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
	fun encodesAndDecodesTypedListAndSetThroughTypes() {
		val registry = testRegistryWith(typedCollectionHolderDefinition())
		val codec = registry.get(TypedCollectionHolder::class.java)
		val holder = TypedCollectionHolder(
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


	// Despite the name, there is no longer such a thing as a nullable `RaptorBsonType` for a top-level field:
	// this reads BSON null through the plain non-null `countryType`, and it is the call-site choice of
	// `valueOrNull` over `valueOrThrow` that permits the null.
	@Test
	fun decodesExplicitBsonNullThroughNullableType() {
		val registry = testRegistryWith(typedNullableHolderDefinition())
		val codec = registry.get(TypedNullableHolder::class.java)

		val actualBytes = codec.encodeTopLevelBytes(TypedNullableHolder(country = null))
		val goldenBytes = documentBytes {
			writeName("country")
			writeNull()
		}

		assertContentEquals(actual = actualBytes, expected = goldenBytes)
		assertEquals(actual = codec.decodeTopLevelValue(goldenBytes), expected = TypedNullableHolder(country = null))
	}


	// Counterpart to the test above: `valueOrNull` on the same non-null `RaptorBsonType` still decodes a present value.
	@Test
	fun decodesNonNullValueThroughNullableType() {
		val registry = testRegistryWith(typedNullableHolderDefinition())
		val codec = registry.get(TypedNullableHolder::class.java)

		val bytes = documentBytes {
			writeName("country")
			writeString("FR")
		}

		assertEquals(
			actual = codec.decodeTopLevelValue(bytes),
			expected = TypedNullableHolder(country = Country.forCode(CountryCode.parse("FR"))),
		)
	}


	// The whole point of the split: a field read with `valueOrThrow` must fail loudly when the stored value is
	// BSON null, instead of handing a null to a non-null property. `TypedScalarHolder` reads both its fields
	// through `valueOrThrow`, so a null `country` has to be rejected.
	@Test
	fun valueOrThrowFailsOnExplicitBsonNull() {
		val registry = testRegistryWith(typedScalarHolderDefinition())
		val codec = registry.get(TypedScalarHolder::class.java)

		val bytes = documentBytes {
			writeName("country")
			writeNull()
			writeName("timestamp")
			writeDateTime(1_705_314_600_000L)
		}

		assertFails {
			codec.decodeTopLevelValue(bytes)
		}
	}


	// Element-level nullability is decided by the element `RaptorBsonType`, not by the call site — so a `List<Country?>`
	// read with `valueOrThrow` still accepts a null *element* while rejecting a null list.
	@Test
	fun decodesNullElementInsideCollectionThroughNullableElementType() {
		val registry = testRegistryWith(typedNullableElementHolderDefinition())
		val codec = registry.get(TypedNullableElementHolder::class.java)
		val holder = TypedNullableElementHolder(
			countries = listOf(Country.forCode(CountryCode.parse("DE")), null, Country.forCode(CountryCode.parse("US"))),
		)

		val goldenBytes = documentBytes {
			writeName("countries")
			writeStartArray()
			writeString("DE")
			writeNull()
			writeString("US")
			writeEndArray()
		}

		assertEquals(actual = codec.decodeTopLevelValue(goldenBytes), expected = holder)
		assertContentEquals(actual = codec.encodeTopLevelBytes(holder), expected = goldenBytes)
	}


	@Test
	fun encodesAndDecodesPrimitivesThroughTypes() {
		val registry = testRegistryWith(typedPrimitiveHolderDefinition())
		val codec = registry.get(TypedPrimitiveHolder::class.java)
		val holder = TypedPrimitiveHolder(
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


	// A primitive `RaptorBsonType` deliberately bypasses the codec registry and reads the reader's own primitive
	// method, so it does not inherit the numeric widening that org.bson's value codecs perform. This is the
	// one observable behavioral difference between the two paths — and the proof that the concrete overload,
	// not the generic one, is what the compiler selects for a `RaptorBsonType<Double>`.
	@Test
	fun primitiveTypeReadsThroughReaderPrimitiveMethodWithoutNumericWidening() {
		val registry = testRegistryWith(typedDoubleHolderDefinition(), genericDoubleHolderDefinition())

		val bytes = documentBytes {
			writeName("value")
			writeInt32(3)
		}

		assertEquals(
			actual = registry.get(GenericDoubleHolder::class.java).decodeTopLevelValue(bytes),
			expected = GenericDoubleHolder(value = 3.0),
		)

		assertFails {
			registry.get(TypedDoubleHolder::class.java).decodeTopLevelValue(bytes)
		}
	}


	// A `RaptorBsonType` reused across two independently built registries must rebind rather than keep serving the
	// codec it resolved from the first one.
	@Test
	fun rebindsToTheCorrectCodecAcrossSeparateRegistries() {
		val registryA = testRegistryWith(rebindProbeDefinition(suffix = "-A"), rebindProbeHolderDefinition())
		val registryB = testRegistryWith(rebindProbeDefinition(suffix = "-B"), rebindProbeHolderDefinition())

		val bytes = documentBytes {
			writeName("probe")
			writeStartDocument()
			writeName("tag")
			writeString("probe")
			writeEndDocument()
		}

		val codecA = registryA.get(RebindProbeHolder::class.java)
		val codecB = registryB.get(RebindProbeHolder::class.java)

		assertEquals(actual = codecA.decodeTopLevelValue(bytes).probe.tag, expected = "probe-A")
		assertEquals(actual = codecB.decodeTopLevelValue(bytes).probe.tag, expected = "probe-B")

		// Query A again after B to prove it rebinds back rather than sticking to B's codec.
		assertEquals(actual = codecA.decodeTopLevelValue(bytes).probe.tag, expected = "probe-A")
	}
}
