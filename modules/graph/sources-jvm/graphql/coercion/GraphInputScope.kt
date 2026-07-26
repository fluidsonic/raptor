package io.fluidsonic.raptor.graph


/** The one and only [RaptorGraphInputScope] — the interface is stateless. */
internal object GraphInputScope : RaptorGraphInputScope {

	override fun invalid(details: String?): Nothing = when (details) {
		null -> invalidValueError()
		else -> invalidValueError("The value is invalid: $details")
	}
}
