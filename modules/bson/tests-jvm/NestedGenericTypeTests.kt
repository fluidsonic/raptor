package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import kotlin.test.*
import tests.utility.*


// A minimal, deliberately generic (not List/Set) wrapper — the shape this generalization targets. `Box` has no
// `collectionKind`, so nothing special-cases it ahead of the codec registry: its own codec is the only place
// that can resolve `T`, and reaching that codec through a `RaptorBsonType` is what hands it a precomputed
// `argumentTypes[0]` instead of a raw `KTypeProjection` to resolve reflectively on every read.
private data class Box<T : Any>(val value: T)


// `Box`'s type parameter is `T : Any`, so `Box<String?>` cannot be written at all. Argument nullability needs a
// wrapper whose parameter admits it — hence a second, unbounded wrapper used only for the nullable cases.
private data class OptionalBox<T>(val value: T)


// Two type arguments, to prove `argumentTypes` is not limited to the single argument `collectionKind` and
// `elementType` hardcode.
private data class Pair2<A : Any, B : Any>(val first: A, val second: B)


// Records which of its two decoders ran. `via` is never written to BSON — it exists purely so a test can tell
// the type-aware path from the reflective one apart by looking at the decoded value.
private data class DispatchProbe<T : Any>(val value: T, val via: String)


private fun boxDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<Box<*>> {
		decodeWithType { type ->
			val valueType = type.argumentTypes.single()
				?: error("Cannot decode a Box whose value type is not statically known: $type")

			var value: Any? = null

			reader.documentByField { field ->
				when (field) {
					"value" -> value = valueOrThrow(valueType)
					else -> skipValue()
				}
			}

			Box(value ?: missingFieldValue("value"))
		}

		decode { arguments ->
			val valueType = arguments?.singleOrNull()?.type
				?: error("Cannot decode a Box whose value type is not statically known.")

			var payload: Any? = null

			reader.documentByField { field ->
				when (field) {
					"value" -> payload = value(valueType)
					else -> skipValue()
				}
			}

			Box(payload ?: missingFieldValue("value"))
		}

		encode { box ->
			writer.document {
				value("value", box.value)
			}
		}
	}


private fun optionalBoxDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<OptionalBox<*>> {
		decodeWithType { type ->
			val valueType = type.argumentTypes.single()
				?: error("Cannot decode an OptionalBox whose value type is not statically known: $type")

			var value: Any? = null
			var valueWasRead = false

			reader.documentByField { field ->
				when (field) {
					"value" -> {
						// Mirrors how `DefaultBsonReaderScope.collectionValue` picks its per-element read: the
						// data shape decides, not the call site.
						value = when (type.argumentNullability.single()) {
							true -> valueOrNull(valueType)
							false -> valueOrThrow(valueType)
						}
						valueWasRead = true
					}

					else -> skipValue()
				}
			}

			if (!valueWasRead)
				missingFieldValue("value")

			OptionalBox(value)
		}

		decode { arguments ->
			val valueType = arguments?.singleOrNull()?.type
				?: error("Cannot decode an OptionalBox whose value type is not statically known.")

			var payload: Any? = null

			reader.documentByField { field ->
				when (field) {
					"value" -> payload = value(valueType)
					else -> skipValue()
				}
			}

			OptionalBox(payload)
		}

		encode { box ->
			writer.document {
				value("value", box.value, preserveNull = true)
			}
		}
	}


private fun pair2Definition(): RaptorBsonDefinition =
	raptor.bson.definition<Pair2<*, *>> {
		decodeWithType { type ->
			val firstType = type.argumentTypes[0]
				?: error("Cannot decode a Pair2 whose first type is not statically known: $type")
			val secondType = type.argumentTypes[1]
				?: error("Cannot decode a Pair2 whose second type is not statically known: $type")

			var first: Any? = null
			var second: Any? = null

			reader.documentByField { field ->
				when (field) {
					"first" -> first = valueOrThrow(firstType)
					"second" -> second = valueOrThrow(secondType)
					else -> skipValue()
				}
			}

			Pair2(
				first = first ?: missingFieldValue("first"),
				second = second ?: missingFieldValue("second"),
			)
		}

		decode { arguments ->
			val firstType = arguments?.getOrNull(0)?.type
				?: error("Cannot decode a Pair2 whose first type is not statically known.")
			val secondType = arguments.getOrNull(1)?.type
				?: error("Cannot decode a Pair2 whose second type is not statically known.")

			var first: Any? = null
			var second: Any? = null

			reader.documentByField { field ->
				when (field) {
					"first" -> first = value(firstType)
					"second" -> second = value(secondType)
					else -> skipValue()
				}
			}

			Pair2(
				first = first ?: missingFieldValue("first"),
				second = second ?: missingFieldValue("second"),
			)
		}

		encode { pair ->
			writer.document {
				value("first", pair.first)
				value("second", pair.second)
			}
		}
	}


private fun dispatchProbeDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<DispatchProbe<*>> {
		decodeWithType { type ->
			val valueType = type.argumentTypes.single()
				?: error("Cannot decode a DispatchProbe whose value type is not statically known: $type")

			var value: Any? = null

			reader.documentByField { field ->
				when (field) {
					"value" -> value = valueOrThrow(valueType)
					else -> skipValue()
				}
			}

			DispatchProbe(value = value ?: missingFieldValue("value"), via = "type")
		}

		decode { arguments ->
			val valueType = arguments?.singleOrNull()?.type
				?: error("Cannot decode a DispatchProbe whose value type is not statically known.")

			var payload: Any? = null

			reader.documentByField { field ->
				when (field) {
					"value" -> payload = value(valueType)
					else -> skipValue()
				}
			}

			DispatchProbe(value = payload ?: missingFieldValue("value"), via = "reflective")
		}

		encode { probe ->
			writer.document {
				value("value", probe.value)
			}
		}
	}


// These are resolved once, at file scope — the intended usage, and the reason `argumentTypes` can be
// precomputed at all.
private val boxOfStringType = raptor.bson.type<Box<String>>()
private val boxOfBoxOfIntType = raptor.bson.type<Box<Box<Int>>>()
private val pair2Type = raptor.bson.type<Pair2<String, Int>>()
private val optionalBoxOfNullableStringType = raptor.bson.type<OptionalBox<String?>>()
private val optionalBoxOfStringType = raptor.bson.type<OptionalBox<String>>()
private val dispatchProbeType = raptor.bson.type<DispatchProbe<String>>()


// `RaptorBsonDefinition` is keyed by the erased `KClass`, so every host holder needs its own concrete class.
private data class BoxOfStringHolder(val box: Box<String>)


private fun boxOfStringHolderDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<BoxOfStringHolder> {
		decode {
			reader.document {
				BoxOfStringHolder(box = valueOrThrow("box", boxOfStringType))
			}
		}

		encode { holder ->
			writer.document {
				value("box", boxOfStringType, holder.box)
			}
		}
	}


private data class BoxOfBoxOfIntHolder(val box: Box<Box<Int>>)


private fun boxOfBoxOfIntHolderDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<BoxOfBoxOfIntHolder> {
		decode {
			reader.document {
				BoxOfBoxOfIntHolder(box = valueOrThrow("box", boxOfBoxOfIntType))
			}
		}

		encode { holder ->
			writer.document {
				value("box", boxOfBoxOfIntType, holder.box)
			}
		}
	}


private data class Pair2Holder(val pair: Pair2<String, Int>)


private fun pair2HolderDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<Pair2Holder> {
		decode {
			reader.document {
				Pair2Holder(pair = valueOrThrow("pair", pair2Type))
			}
		}

		encode { holder ->
			writer.document {
				value("pair", pair2Type, holder.pair)
			}
		}
	}


private data class NullableArgumentHolder(val box: OptionalBox<String?>)


private fun nullableArgumentHolderDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<NullableArgumentHolder> {
		decode {
			reader.document {
				NullableArgumentHolder(box = valueOrThrow("box", optionalBoxOfNullableStringType))
			}
		}

		encode { holder ->
			writer.document {
				value("box", optionalBoxOfNullableStringType, holder.box)
			}
		}
	}


private data class NonNullArgumentHolder(val box: OptionalBox<String>)


private fun nonNullArgumentHolderDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<NonNullArgumentHolder> {
		decode {
			reader.document {
				NonNullArgumentHolder(box = valueOrThrow("box", optionalBoxOfStringType))
			}
		}

		encode { holder ->
			writer.document {
				value("box", optionalBoxOfStringType, holder.box)
			}
		}
	}


private data class DispatchProbeHolder(val probe: DispatchProbe<String>)


// Reads the same probe twice from the same document — once through a `RaptorBsonType`, once through the reflective
// `reader.value<T>()` path — so a single decode can show which decoder each path selected.
private fun dispatchProbeHolderDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<DispatchProbeHolder> {
		decode {
			reader.document {
				DispatchProbeHolder(probe = valueOrThrow("probe", dispatchProbeType))
			}
		}

		encode { holder ->
			writer.document {
				value("probe", dispatchProbeType, holder.probe)
			}
		}
	}


private data class ReflectiveProbeHolder(val probe: DispatchProbe<String>)


private fun reflectiveProbeHolderDefinition(): RaptorBsonDefinition =
	raptor.bson.definition<ReflectiveProbeHolder> {
		decode {
			reader.document {
				ReflectiveProbeHolder(probe = value("probe"))
			}
		}

		encode { holder ->
			writer.document {
				value("probe", holder.probe)
			}
		}
	}


private fun probeBytes(value: String): ByteArray =
	documentBytes {
		writeName("probe")
		writeStartDocument()
		writeName("value")
		writeString(value)
		writeEndDocument()
	}


class NestedGenericTypeTests {

	// Scenario 1 — one level of nesting: the wrapped `String` is resolved through `argumentTypes[0]`, which
	// binds the registry's own `String` codec once for the whole `Box<String>` type.
	@Test
	fun encodesAndDecodesGenericWrapperWithCodecBackedArgument() {
		val registry = testRegistryWith(boxDefinition(), boxOfStringHolderDefinition())
		val codec = registry.get(BoxOfStringHolder::class.java)
		val holder = BoxOfStringHolder(box = Box("hello"))

		val goldenBytes = documentBytes {
			writeName("box")
			writeStartDocument()
			writeName("value")
			writeString("hello")
			writeEndDocument()
		}

		assertContentEquals(actual = codec.encodeTopLevelBytes(holder), expected = goldenBytes)
		assertEquals(actual = codec.decodeTopLevelValue(goldenBytes), expected = holder)
	}


	// Scenario 2 — two levels: the outer `Box<Box<Int>>` type.s `argumentTypes[0]` is itself a `RaptorBsonType` whose
	// OWN `argumentTypes[0]` resolves the inner `Int`, so the recursion has to hold for this to decode.
	@Test
	fun encodesAndDecodesTwoLevelsOfGenericNesting() {
		val registry = testRegistryWith(boxDefinition(), boxOfBoxOfIntHolderDefinition())
		val codec = registry.get(BoxOfBoxOfIntHolder::class.java)
		val holder = BoxOfBoxOfIntHolder(box = Box(Box(42)))

		val goldenBytes = documentBytes {
			writeName("box")
			writeStartDocument()
			writeName("value")
			writeStartDocument()
			writeName("value")
			writeInt32(42)
			writeEndDocument()
			writeEndDocument()
		}

		assertContentEquals(actual = codec.encodeTopLevelBytes(holder), expected = goldenBytes)
		assertEquals(actual = codec.decodeTopLevelValue(goldenBytes), expected = holder)
	}


	// Scenario 3 — two type arguments, each with its own precomputed `RaptorBsonType`.
	@Test
	fun encodesAndDecodesGenericTypeWithTwoArguments() {
		val registry = testRegistryWith(pair2Definition(), pair2HolderDefinition())
		val codec = registry.get(Pair2Holder::class.java)
		val holder = Pair2Holder(pair = Pair2(first = "answer", second = 42))

		val goldenBytes = documentBytes {
			writeName("pair")
			writeStartDocument()
			writeName("first")
			writeString("answer")
			writeName("second")
			writeInt32(42)
			writeEndDocument()
		}

		assertContentEquals(actual = codec.encodeTopLevelBytes(holder), expected = goldenBytes)
		assertEquals(actual = codec.decodeTopLevelValue(goldenBytes), expected = holder)
	}


	// Scenario 4a — `OptionalBox<String?>`: `argumentNullability[0]` is `true`, so the codec reads the argument
	// with `valueOrNull` and a stored BSON null decodes to Kotlin null.
	@Test
	fun decodesBsonNullForNullableTypeArgument() {
		val registry = testRegistryWith(optionalBoxDefinition(), nullableArgumentHolderDefinition())
		val codec = registry.get(NullableArgumentHolder::class.java)
		val holder = NullableArgumentHolder(box = OptionalBox(null))

		val goldenBytes = documentBytes {
			writeName("box")
			writeStartDocument()
			writeName("value")
			writeNull()
			writeEndDocument()
		}

		assertContentEquals(actual = codec.encodeTopLevelBytes(holder), expected = goldenBytes)
		assertEquals(actual = codec.decodeTopLevelValue(goldenBytes), expected = holder)
	}


	// Scenario 4b — the same nullable-argument codec still decodes a present value.
	@Test
	fun decodesPresentValueForNullableTypeArgument() {
		val registry = testRegistryWith(optionalBoxDefinition(), nullableArgumentHolderDefinition())
		val codec = registry.get(NullableArgumentHolder::class.java)

		val bytes = documentBytes {
			writeName("box")
			writeStartDocument()
			writeName("value")
			writeString("present")
			writeEndDocument()
		}

		assertEquals(
			actual = codec.decodeTopLevelValue(bytes),
			expected = NullableArgumentHolder(box = OptionalBox("present")),
		)
	}


	// Scenario 4c — the counterpart that proves `argumentNullability` is actually consulted rather than the
	// codec simply always tolerating null: the very same codec, reached through an `OptionalBox<String>` type
	// instead, must reject the identical bytes.
	@Test
	fun rejectsBsonNullForNonNullTypeArgument() {
		val registry = testRegistryWith(optionalBoxDefinition(), nonNullArgumentHolderDefinition())
		val codec = registry.get(NonNullArgumentHolder::class.java)

		val bytes = documentBytes {
			writeName("box")
			writeStartDocument()
			writeName("value")
			writeNull()
			writeEndDocument()
		}

		assertFails {
			codec.decodeTopLevelValue(bytes)
		}
	}


	// The dispatch itself: reached through a `RaptorBsonType`, a `RaptorBsonTypeAwareCodec` must decode through
	// its type-aware method rather than falling back to the reflective `KTypeProjection` one.
	@Test
	fun decodesThroughTypeAwareMethodWhenReachedThroughAType() {
		val registry = testRegistryWith(dispatchProbeDefinition(), dispatchProbeHolderDefinition())
		val codec = registry.get(DispatchProbeHolder::class.java)

		assertEquals(
			actual = codec.decodeTopLevelValue(probeBytes("x")).probe,
			expected = DispatchProbe(value = "x", via = "type"),
		)
	}


	// …and the base `decode(arguments)` must still work standalone for the reflective path, which has no
	// `RaptorBsonType` to offer.
	@Test
	fun decodesThroughBaseMethodWhenReachedThroughTypeArguments() {
		val registry = testRegistryWith(dispatchProbeDefinition(), reflectiveProbeHolderDefinition())
		val codec = registry.get(ReflectiveProbeHolder::class.java)

		assertEquals(
			actual = codec.decodeTopLevelValue(probeBytes("x")).probe,
			expected = DispatchProbe(value = "x", via = "reflective"),
		)
	}


	// `argumentTypes` is one entry per type argument, `null` where a star projection leaves the argument's
	// type unknown — and it recurses, so a nested argument carries its own resolved sub-types.
	@Test
	fun resolvesOneArgumentTypePerTypeArgument() {
		assertEquals(actual = boxOfStringType.argumentTypes.size, expected = 1)
		assertEquals(actual = pair2Type.argumentTypes.size, expected = 2)
		assertNotNull(boxOfBoxOfIntType.argumentTypes.single()).let { innerType ->
			assertEquals(actual = innerType.argumentTypes.size, expected = 1)
			assertNotNull(innerType.argumentTypes.single())
		}
		assertNull(raptor.bson.type<Box<*>>().argumentTypes.single())
		assertEquals(actual = raptor.bson.type<Box<*>>().argumentNullability, expected = listOf(false))
	}


	@Test
	fun resolvesNullabilityPerTypeArgument() {
		assertEquals(actual = optionalBoxOfNullableStringType.argumentNullability, expected = listOf(true))
		assertEquals(actual = optionalBoxOfStringType.argumentNullability, expected = listOf(false))
		assertEquals(actual = pair2Type.argumentNullability, expected = listOf(false, false))
		assertEquals(actual = raptor.bson.type<Pair2<String, Int>>().argumentNullability, expected = listOf(false, false))
		assertEquals(actual = raptor.bson.type<OptionalBox<Box<String>?>>().argumentNullability, expected = listOf(true))
	}
}
