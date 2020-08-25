@file:JvmName("Money@extension")

package io.fluidsonic.raptor

import io.fluidsonic.currency.*


internal fun Money.Companion.bsonDefinition(): RaptorBsonDefinitions = bsonDefinition<Money> {
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


internal fun Money.Companion.graphDefinition(): RaptorGraphDefinition = graphScalarDefinition {
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
