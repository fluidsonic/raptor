// https://youtrack.jetbrains.com/issue/KT-12495
@file:JvmName("Money@graph")

package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*


public fun Money.Companion.graphDefinition(): GraphScalarDefinition<Money> = graphScalarDefinition {
	parseObject { value ->
		if (value.size != 2)
			invalid()

		val amount = (value["amount"] as? String)
			?.toLongOrNull()
			?.let(::Cents)
			?: invalid()

		val currency = (value["currency"] as? String)
			?.let(Currency::byCode)
			?: invalid()

		Money(amount = amount, currency = currency)
	}

	// FIXME add parseJsonMap…? support codecs? how to reuse other fn?
	parseJson<Map<String, *>> { value ->
		if (value.size != 2)
			invalid()

		val amount = (value["amount"] as? String)
			?.toLongOrNull()
			?.let(::Cents)
			?: invalid()

		val currency = (value["currency"] as? String)
			?.let(Currency::byCode)
			?: invalid()

		Money(amount = amount, currency = currency)
	}

	serializeJson { value: Money ->
		mapOf(
			"amount" to value.amount.value.toString(),
			"currency" to value.currency.code
		)
	}
}
