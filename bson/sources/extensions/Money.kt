package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*


fun Money.Companion.bsonDefinition() = bsonDefinition<Money> {
	decode {
		var amount: Cents? = null
		var currency: Currency? = null

		readDocumentWithValues { fieldName ->
			when (fieldName) {
				"amount" -> amount = readValueOfType(Cents::class)
				"currency" -> currency = readValueOfType(Currency::class)
				else -> skipValue()
			}
		}

		Money(
			amount = amount ?: throw BsonException("missing amount"),
			currency = currency ?: throw BsonException("missing currency")
		)
	}

	encode { value ->
		writeDocument {
			write(name = "amount", value = value.amount)
			write(name = "currency", value = value.currency)
		}
	}
}
