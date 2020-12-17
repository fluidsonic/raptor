package tests

import io.fluidsonic.raptor.*


class TextCollectionComponent : RaptorComponent.Default<TextCollectionComponent>() {

	var _text = ""


	override fun toString() =
		"text collection ($_text)"


	override fun RaptorComponentConfigurationEndScope.onConfigurationEnded() {
		propertyRegistry.register(TextRaptorPropertyKey, _text)
	}


	object Key : RaptorComponentKey<TextCollectionComponent> {

		override fun toString() = "text collection"
	}
}


@RaptorDsl
fun RaptorComponentSet<TextCollectionComponent>.append(fragment: String) = configure {
	_text += fragment
}


@RaptorDsl
val RaptorTopLevelConfigurationScope.textCollection
	get() = componentRegistry.configure(TextCollectionComponent.Key)
