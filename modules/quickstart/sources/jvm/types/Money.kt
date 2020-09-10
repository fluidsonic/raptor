package io.fluidsonic.raptor

import io.fluidsonic.currency.*


public data class Money(
	val amount: Cents,
	val currency: Currency,
) {

	public companion object {

		public fun bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<Money> {
			decode {
				var amount: Cents? = null
				var currency: Currency? = null

				reader.documentByField { fieldName ->
					when (fieldName) {
						"amount" -> amount = value()
						"currency" -> currency = value()
						else -> skipValue()
					}
				}

				Money(
					amount = amount ?: error("missing amount"),
					currency = currency ?: error("missing currency")
				)
			}

			encode { value ->
				writer.document {
					value("amount", value.amount)
					value("currency", value.currency)
				}
			}
		}


		public fun graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
			parseObject { value ->
				if (value.size != 2)
					invalid()

				val amount = (value["amount"] as? String)
					?.toLongOrNull()
					?.let(::Cents)
					?: invalid()

				val currency = (value["currency"] as? String)
					?.let(Currency::forCodeOrNull)
					?: invalid()

				Money(amount = amount, currency = currency)
			}

			serialize { value: Money ->
				mapOf(
					"amount" to value.amount.value.toString(),
					"currency" to value.currency.code
				)
			}
		}
	}
}
