package io.fluidsonic.raptor

import io.fluidsonic.currency.*


data class Money(
	val amount: Cents,
	val currency: Currency
) {

	companion object
}
