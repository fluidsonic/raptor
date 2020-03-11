// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Money@graph")

package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*


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
