package tests

import io.fluidsonic.raptor.*


class BasicComponent : RaptorComponent.Base<BasicComponent>() {

	var _text = ""


	override fun toString() =
		"BasicComponent(text=$_text)"
}


@RaptorDsl
fun RaptorComponentSet<BasicComponent>.append(fragment: String) = forEach {
	_text += fragment
}
