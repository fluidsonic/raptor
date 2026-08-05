package tests

import kotlin.test.*
import tests.utility.*


// The methodology gate for every hand-written codec in the benchmark suite.
//
// A benchmark's hand-written codec only stands in for a genki-core codec if it decodes the way genki-core
// actually has to: by field name, in whatever order the wire happens to carry. genki-core replays events
// written by many code versions over time, so no wire order can ever be trusted — the same justification
// `MinimalKotlinxSerializationBsonTests.documentFieldsAreMatchedByNameRegardlessOfWireOrder` records for the
// `RaptorBsonSerializationCodec` format, and the reason `RaptorAggregateEventBson` decodes through
// `reader.documentByField { }` rather than a positional `reader.document { }` sequence.
//
// Every document below is hand-built in an order that matches NO encoder in this repo, so a decoder that
// silently regressed to positional reads would fail here. A round-trip test cannot catch that: encode's own
// field order would happen to be exactly the order a positional decoder expects.
class PositionIndependentDecodingTests {

	private val wideCodec = testRegistryWith(wideDocumentDefinition).get(WideDocument::class.java)
	private val typedWideCodec = testRegistryWith(typedWideDocumentDefinition).get(TypedWideDocument::class.java)

	private val complexOrderRegistry = testRegistryWith(
		customerIdDefinition,
		centsDefinition,
		addressDefinition,
		customerDefinition,
		orderItemDefinition,
		complexOrderDefinition,
	)

	private val complexOrderCodec = complexOrderRegistry.get(ComplexOrder::class.java)


	// `e, h, b, g, a, f, d, c` — deliberately unlike the encoder's `a … h`, and unlike any reversal of it.
	private val scrambledWideDocumentBytes = documentBytes {
		writeName("e")
		writeString("hello world")
		writeName("h")
		writeStartArray()
		writeString("a")
		writeString("b")
		writeString("c")
		writeEndArray()
		writeName("b")
		writeInt64(42_000_000_000L)
		writeName("g")
		writeString("DE")
		writeName("a")
		writeInt32(42)
		writeName("f")
		writeDateTime(1_705_314_600_000L)
		writeName("d")
		writeBoolean(true)
		writeName("c")
		writeDouble(3.14159)
	}


	@Test
	fun wideDocumentDecodesFieldsInAnyWireOrder() {
		assertEquals(actual = wideCodec.decodeTopLevelValue(scrambledWideDocumentBytes), expected = wideDocumentSample)
	}


	@Test
	fun typedWideDocumentDecodesFieldsInAnyWireOrder() {
		assertEquals(
			actual = typedWideCodec.decodeTopLevelValue(scrambledWideDocumentBytes),
			expected = typedWideDocumentSample,
		)
	}


	// The other half of the same requirement: fields dropped from the Kotlin class long ago are still present
	// in historical documents and must be skipped, not rejected.
	@Test
	fun wideDocumentSkipsUnknownWireFields() {
		val bytesWithExtraFields = documentBytes {
			writeName("removedLongAgo")
			writeString("junk")
			writeName("a")
			writeInt32(42)
			writeName("b")
			writeInt64(42_000_000_000L)
			writeName("c")
			writeDouble(3.14159)
			writeName("alsoGone")
			writeStartDocument()
			writeName("nested")
			writeInt32(7)
			writeEndDocument()
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
			writeName("trailingExtra")
			writeStartArray()
			writeInt32(1)
			writeInt32(2)
			writeEndArray()
		}

		assertEquals(actual = wideCodec.decodeTopLevelValue(bytesWithExtraFields), expected = wideDocumentSample)
	}


	@Test
	fun wideDocumentFailsWhenARequiredFieldIsMissing() {
		val bytesMissingC = documentBytes {
			writeName("a")
			writeInt32(42)
			writeName("b")
			writeInt64(42_000_000_000L)
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
			writeEndArray()
		}

		// Asserting the message, not merely the exception type: a positional decoder also throws
		// `IllegalStateException` here, but from the field-name `check()` rather than the missing-field check
		// this test is about.
		assertEquals(
			actual = assertFailsWith<IllegalStateException> { wideCodec.decodeTopLevelValue(bytesMissingC) }.message,
			expected = "BSON object requires a value for field 'c'.",
		)
	}


	// Every nesting level is scrambled independently, and each of the three order items uses a different
	// permutation — so no single positional sequence could decode this document even by accident.
	@Test
	fun complexOrderDecodesFieldsInAnyWireOrderAtEveryNestingLevel() {
		val scrambledBytes = documentBytes {
			writeName("placedAt")
			writeDateTime(1_705_314_600_000L)
			writeName("items")
			writeStartArray()
			// unitPrice, sku, quantity
			writeStartDocument()
			writeName("unitPrice")
			writeInt64(4_999L)
			writeName("sku")
			writeString("SKU-ALPHA-001")
			writeName("quantity")
			writeInt32(777)
			writeEndDocument()
			// quantity, unitPrice, sku
			writeStartDocument()
			writeName("quantity")
			writeInt32(888)
			writeName("unitPrice")
			writeInt64(12_345L)
			writeName("sku")
			writeString("SKU-BETA-002")
			writeEndDocument()
			// sku, unitPrice, quantity
			writeStartDocument()
			writeName("sku")
			writeString("SKU-GAMMA-003")
			writeName("unitPrice")
			writeInt64(999_000L)
			writeName("quantity")
			writeInt32(999)
			writeEndDocument()
			writeEndArray()
			writeName("notes")
			writeString("leave at the front desk")
			writeName("orderId")
			writeString("order-2c7be4")
			writeName("totalAmount")
			writeInt64(1_016_344L)
			// customer: address, loyaltyPoints, id, name — and address itself: zipCode, street, city
			writeName("customer")
			writeStartDocument()
			writeName("address")
			writeStartDocument()
			writeName("zipCode")
			writeString("10115")
			writeName("street")
			writeString("Bahnhofstraße 12")
			writeName("city")
			writeString("Berlin")
			writeEndDocument()
			writeName("loyaltyPoints")
			writeInt32(54_321)
			writeName("id")
			writeString("cust-8f3a91")
			writeName("name")
			writeString("Erika Mustermann")
			writeEndDocument()
		}

		assertEquals(actual = complexOrderCodec.decodeTopLevelValue(scrambledBytes), expected = complexOrderSample)
	}


	@Test
	fun complexOrderSkipsUnknownWireFields() {
		val bytesWithExtraFields = documentBytes {
			writeName("schemaVersion")
			writeInt32(3)
			writeName("orderId")
			writeString("order-2c7be4")
			writeName("customer")
			writeStartDocument()
			writeName("id")
			writeString("cust-8f3a91")
			writeName("name")
			writeString("Erika Mustermann")
			writeName("migratedFrom")
			writeString("legacy")
			writeName("address")
			writeStartDocument()
			writeName("street")
			writeString("Bahnhofstraße 12")
			writeName("country")
			writeString("DE")
			writeName("city")
			writeString("Berlin")
			writeName("zipCode")
			writeString("10115")
			writeEndDocument()
			writeName("loyaltyPoints")
			writeInt32(54_321)
			writeEndDocument()
			writeName("items")
			writeStartArray()
			writeStartDocument()
			writeName("sku")
			writeString("SKU-ALPHA-001")
			writeName("discontinued")
			writeBoolean(false)
			writeName("quantity")
			writeInt32(777)
			writeName("unitPrice")
			writeInt64(4_999L)
			writeEndDocument()
			writeStartDocument()
			writeName("sku")
			writeString("SKU-BETA-002")
			writeName("quantity")
			writeInt32(888)
			writeName("unitPrice")
			writeInt64(12_345L)
			writeEndDocument()
			writeStartDocument()
			writeName("sku")
			writeString("SKU-GAMMA-003")
			writeName("quantity")
			writeInt32(999)
			writeName("unitPrice")
			writeInt64(999_000L)
			writeEndDocument()
			writeEndArray()
			writeName("totalAmount")
			writeInt64(1_016_344L)
			writeName("placedAt")
			writeDateTime(1_705_314_600_000L)
			writeName("notes")
			writeString("leave at the front desk")
			writeName("internalAudit")
			writeStartArray()
			writeString("checked")
			writeEndArray()
		}

		assertEquals(actual = complexOrderCodec.decodeTopLevelValue(bytesWithExtraFields), expected = complexOrderSample)
	}


	@Test
	fun complexOrderFailsWhenARequiredFieldIsMissing() {
		val bytesMissingTotalAmount = documentBytes {
			writeName("orderId")
			writeString("order-2c7be4")
			writeName("customer")
			writeStartDocument()
			writeName("id")
			writeString("cust-8f3a91")
			writeName("name")
			writeString("Erika Mustermann")
			writeName("address")
			writeStartDocument()
			writeName("street")
			writeString("Bahnhofstraße 12")
			writeName("city")
			writeString("Berlin")
			writeName("zipCode")
			writeString("10115")
			writeEndDocument()
			writeName("loyaltyPoints")
			writeInt32(54_321)
			writeEndDocument()
			writeName("items")
			writeStartArray()
			writeEndArray()
			writeName("placedAt")
			writeDateTime(1_705_314_600_000L)
			writeName("notes")
			writeString("leave at the front desk")
		}

		assertEquals(
			actual = assertFailsWith<IllegalStateException> {
				complexOrderCodec.decodeTopLevelValue(bytesMissingTotalAmount)
			}.message,
			expected = "BSON object requires a value for field 'totalAmount'.",
		)
	}


	// `notes` is the one genuinely nullable property, so an absent `notes` must decode to `null` rather than
	// fail the missing-field check the required properties get.
	@Test
	fun complexOrderTreatsAnAbsentNullablePropertyAsNull() {
		val bytesWithoutNotes = documentBytes {
			writeName("totalAmount")
			writeInt64(1_016_344L)
			writeName("orderId")
			writeString("order-2c7be4")
			writeName("placedAt")
			writeDateTime(1_705_314_600_000L)
			writeName("items")
			writeStartArray()
			writeStartDocument()
			writeName("sku")
			writeString("SKU-ALPHA-001")
			writeName("quantity")
			writeInt32(777)
			writeName("unitPrice")
			writeInt64(4_999L)
			writeEndDocument()
			writeStartDocument()
			writeName("sku")
			writeString("SKU-BETA-002")
			writeName("quantity")
			writeInt32(888)
			writeName("unitPrice")
			writeInt64(12_345L)
			writeEndDocument()
			writeStartDocument()
			writeName("sku")
			writeString("SKU-GAMMA-003")
			writeName("quantity")
			writeInt32(999)
			writeName("unitPrice")
			writeInt64(999_000L)
			writeEndDocument()
			writeEndArray()
			writeName("customer")
			writeStartDocument()
			writeName("id")
			writeString("cust-8f3a91")
			writeName("name")
			writeString("Erika Mustermann")
			writeName("address")
			writeStartDocument()
			writeName("street")
			writeString("Bahnhofstraße 12")
			writeName("city")
			writeString("Berlin")
			writeName("zipCode")
			writeString("10115")
			writeEndDocument()
			writeName("loyaltyPoints")
			writeInt32(54_321)
			writeEndDocument()
		}

		assertEquals(
			actual = complexOrderCodec.decodeTopLevelValue(bytesWithoutNotes),
			expected = complexOrderWithoutNotesSample,
		)
	}
}
