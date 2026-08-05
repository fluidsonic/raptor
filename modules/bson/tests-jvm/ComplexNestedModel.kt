package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import io.fluidsonic.time.*


// TEST FIXTURE — not wired into any production path. A deliberately *complex* record shape, in contrast to
// the flat eight-field `WideDocument` shape in `WideDocumentModel.kt`: three levels of object nesting, a
// nested `List` of nested objects, two `@JvmInline value class` types, and one nullable field. Its purpose is
// to give `PositionIndependentDecodingTests` a hierarchy deep enough that every nesting level can be
// scrambled independently.
//
// Every decode block below dispatches by field name through `reader.documentByField { }`, never as a
// positional `reader.document { }` sequence — see the explanation at the top of `WideDocumentModel.kt` and
// the proof in `PositionIndependentDecodingTests`. The `encode` blocks stay positional on purpose.


/** A `String`-backed inline value class, exercising how each codec path handles inline-class boxing. */
@JvmInline
internal value class CustomerId(val value: String)


/** A `Long`-backed inline value class, the primitive-backed counterpart to [CustomerId]. */
@JvmInline
internal value class Cents(val value: Long)


internal data class Address(
	val street: String,
	val city: String,
	val zipCode: String,
)


internal data class Customer(
	val id: CustomerId,
	val name: String,
	val address: Address,
	val loyaltyPoints: Int,
)


internal data class OrderItem(
	val sku: String,
	val quantity: Int,
	val unitPrice: Cents,
)


internal data class ComplexOrder(
	val orderId: CustomerId,
	val customer: Customer,
	val items: List<OrderItem>,
	val totalAmount: Cents,
	val placedAt: Timestamp,
	val notes: String?,
)


// One `RaptorBsonType` per reused type, resolved once at file scope — the intended `raptor.bson.type<T>()`
// usage, and the only way this path can be faster than the reified `reader.value<T>(field)` path it is
// compared against. `List<OrderItem>` gets its own one so the nested list reuses one precomputed element type
// instead of re-dispatching per element.
private val stringType = raptor.bson.type<String>()
private val intType = raptor.bson.type<Int>()
private val customerIdType = raptor.bson.type<CustomerId>()
private val centsType = raptor.bson.type<Cents>()
private val addressType = raptor.bson.type<Address>()
private val customerType = raptor.bson.type<Customer>()
private val orderItemListType = raptor.bson.type<List<OrderItem>>()
private val timestampType = raptor.bson.type<Timestamp>()


// Both value classes follow the `kotlin.time.Duration` precedent in
// `modules/bson/sources-jvm/extensions/Duration.kt`: the wire value is the wrapped scalar, never a wrapper
// document — which is also what `kotlinx.serialization` writes for an inline class, making the two
// hierarchies byte-comparable.
@Suppress("RemoveExplicitTypeArguments")
internal val customerIdDefinition: RaptorBsonDefinition =
	raptor.bson.definition<CustomerId> {
		decode<String>(::CustomerId)
		encode(CustomerId::value)
	}


@Suppress("RemoveExplicitTypeArguments")
internal val centsDefinition: RaptorBsonDefinition =
	raptor.bson.definition<Cents> {
		decode<Long>(::Cents)
		encode(Cents::value)
	}


internal val addressDefinition: RaptorBsonDefinition =
	raptor.bson.definition<Address> {
		decode {
			var street: String? = null
			var city: String? = null
			var zipCode: String? = null

			reader.documentByField { field ->
				when (field) {
					"street" -> street = valueOrThrow(stringType)
					"city" -> city = valueOrThrow(stringType)
					"zipCode" -> zipCode = valueOrThrow(stringType)
					else -> skipValue()
				}
			}

			Address(
				street = street ?: missingFieldValue("street"),
				city = city ?: missingFieldValue("city"),
				zipCode = zipCode ?: missingFieldValue("zipCode"),
			)
		}

		encode { address ->
			writer.document {
				value("street", stringType, address.street)
				value("city", stringType, address.city)
				value("zipCode", stringType, address.zipCode)
			}
		}
	}


internal val customerDefinition: RaptorBsonDefinition =
	raptor.bson.definition<Customer> {
		decode {
			var id: CustomerId? = null
			var name: String? = null
			var address: Address? = null
			var loyaltyPoints: Int? = null

			reader.documentByField { field ->
				when (field) {
					"id" -> id = valueOrThrow(customerIdType)
					"name" -> name = valueOrThrow(stringType)
					"address" -> address = valueOrThrow(addressType)
					"loyaltyPoints" -> loyaltyPoints = valueOrThrow(intType)
					else -> skipValue()
				}
			}

			Customer(
				id = id ?: missingFieldValue("id"),
				name = name ?: missingFieldValue("name"),
				address = address ?: missingFieldValue("address"),
				loyaltyPoints = loyaltyPoints ?: missingFieldValue("loyaltyPoints"),
			)
		}

		encode { customer ->
			writer.document {
				value("id", customerIdType, customer.id)
				value("name", stringType, customer.name)
				value("address", addressType, customer.address)
				value("loyaltyPoints", intType, customer.loyaltyPoints)
			}
		}
	}


internal val orderItemDefinition: RaptorBsonDefinition =
	raptor.bson.definition<OrderItem> {
		decode {
			var sku: String? = null
			var quantity: Int? = null
			var unitPrice: Cents? = null

			reader.documentByField { field ->
				when (field) {
					"sku" -> sku = valueOrThrow(stringType)
					"quantity" -> quantity = valueOrThrow(intType)
					"unitPrice" -> unitPrice = valueOrThrow(centsType)
					else -> skipValue()
				}
			}

			OrderItem(
				sku = sku ?: missingFieldValue("sku"),
				quantity = quantity ?: missingFieldValue("quantity"),
				unitPrice = unitPrice ?: missingFieldValue("unitPrice"),
			)
		}

		encode { item ->
			writer.document {
				value("sku", stringType, item.sku)
				value("quantity", intType, item.quantity)
				value("unitPrice", centsType, item.unitPrice)
			}
		}
	}


internal val complexOrderDefinition: RaptorBsonDefinition =
	raptor.bson.definition<ComplexOrder> {
		decode {
			var orderId: CustomerId? = null
			var customer: Customer? = null
			var items: List<OrderItem>? = null
			var totalAmount: Cents? = null
			var placedAt: Timestamp? = null
			var notes: String? = null

			reader.documentByField { field ->
				when (field) {
					"orderId" -> orderId = valueOrThrow(customerIdType)
					"customer" -> customer = valueOrThrow(customerType)
					"items" -> items = valueOrThrow(orderItemListType)
					"totalAmount" -> totalAmount = valueOrThrow(centsType)
					"placedAt" -> placedAt = valueOrThrow(timestampType)
					// The one field read through `valueOrNull` rather than `valueOrThrow`, matching
					// `preserveNull = true` on the write side below.
					"notes" -> notes = valueOrNull(stringType)
					else -> skipValue()
				}
			}

			ComplexOrder(
				orderId = orderId ?: missingFieldValue("orderId"),
				customer = customer ?: missingFieldValue("customer"),
				items = items ?: missingFieldValue("items"),
				totalAmount = totalAmount ?: missingFieldValue("totalAmount"),
				placedAt = placedAt ?: missingFieldValue("placedAt"),
				// The only property with no missing-field check: `notes` is genuinely nullable, so an absent
				// field and an explicit BSON null both legitimately mean `null` here.
				notes = notes,
			)
		}

		encode { order ->
			writer.document {
				value("orderId", customerIdType, order.orderId)
				value("customer", customerType, order.customer)
				value("items", orderItemListType, order.items)
				value("totalAmount", centsType, order.totalAmount)
				value("placedAt", timestampType, order.placedAt)
				value("notes", stringType, order.notes, preserveNull = true)
			}
		}
	}


// The value every scrambled document in `PositionIndependentDecodingTests` must decode to.
internal val complexOrderSample: ComplexOrder = ComplexOrder(
	orderId = CustomerId("order-2c7be4"),
	customer = Customer(
		id = CustomerId("cust-8f3a91"),
		name = "Erika Mustermann",
		address = Address(
			street = "Bahnhofstraße 12",
			city = "Berlin",
			zipCode = "10115",
		),
		loyaltyPoints = 54_321,
	),
	items = listOf(
		OrderItem(sku = "SKU-ALPHA-001", quantity = 777, unitPrice = Cents(4_999L)),
		OrderItem(sku = "SKU-BETA-002", quantity = 888, unitPrice = Cents(12_345L)),
		OrderItem(sku = "SKU-GAMMA-003", quantity = 999, unitPrice = Cents(999_000L)),
	),
	totalAmount = Cents(1_016_344L),
	placedAt = Timestamp.fromEpochMilliseconds(1_705_314_600_000L),
	notes = "leave at the front desk",
)


// Only for the nullable-field test: the sample above holds a real `notes` string, so it cannot double as the
// expected value for a document that omits the field entirely.
internal val complexOrderWithoutNotesSample: ComplexOrder =
	complexOrderSample.copy(notes = null)
