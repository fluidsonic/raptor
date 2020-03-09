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


fun Money.Companion.graphDefinition() = graphScalarDefinition {
	conversion<Money> {
		parseObject { value ->
			if (value.size != 2)
				return@parseObject null

			val amount = (value["amount"] as? String)
				?.toLongOrNull()
				?.let(::Cents)
				?: return@parseObject null

			val currency = (value["currency"] as? String)
				?.let(Currency::byCode)
				?: return@parseObject null

			Money(amount = amount, currency = currency)
		}

		// FIXME add parseJsonMap…? support codecs? how to reuse other fn?
		parseJson<Map<String, *>> { value ->
			if (value.size != 2)
				return@parseJson null

			val amount = (value["amount"] as? String)
				?.toLongOrNull()
				?.let(::Cents)
				?: return@parseJson null

			val currency = (value["currency"] as? String)
				?.let(Currency::byCode)
				?: return@parseJson null

			Money(amount = amount, currency = currency)
		}

		serializeJson { value: Money ->
			mapOf(
				"amount" to value.amount.value.toString(),
				"currency" to value.currency.code
			)
		}
	}
}
