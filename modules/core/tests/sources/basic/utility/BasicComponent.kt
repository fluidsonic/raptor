package tests

import io.fluidsonic.raptor.*


class BasicComponent : RaptorComponent<BasicComponent> {

	var text = ""


	override fun toString() =
		"BasicComponent(text=$text)"
}


@RaptorDsl
fun RaptorComponentSet<BasicComponent>.append(fragment: String) = forEach {
	text += fragment
}
