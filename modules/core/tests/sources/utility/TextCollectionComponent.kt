package tests

import io.fluidsonic.raptor.*


class TextCollectionComponent : RaptorComponent.Base<TextCollectionComponent>() {

	var _text = ""


	fun finalize() =
		_text


	override fun toString() =
		"text collection ($_text)"


	object Key : RaptorComponentKey<TextCollectionComponent> {

		override fun toString() = "text collection"
	}
}


@RaptorDsl
fun RaptorComponentSet<TextCollectionComponent>.append(fragment: String) = configure {
	_text += fragment
}


@RaptorDsl
val RaptorGlobalConfigurationScope.textCollection
	get() = componentRegistry.configure(TextCollectionComponent.Key)
