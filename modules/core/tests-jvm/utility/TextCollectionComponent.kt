package tests

import io.fluidsonic.raptor.*


class TextCollectionComponent : RaptorComponent.Base<TextCollectionComponent>() {

	private var text = ""


	@RaptorDsl
	fun append(fragment: String) {
		text += fragment
	}


	override fun toString() =
		"text collection ($text)"


	override fun RaptorComponentConfigurationEndScope<TextCollectionComponent>.onConfigurationEnded() {
		propertyRegistry.register(textPropertyKey, text)
	}


	companion object {

		val key = RaptorComponentKey<TextCollectionComponent>("text collection")
	}
}


@RaptorDsl
fun RaptorAssemblyQuery<TextCollectionComponent>.append(fragment: String) {
	this {
		append(fragment)
	}
}


@RaptorDsl
val RaptorAssemblyScope.textCollection
	get() = componentRegistry.all(TextCollectionComponent.key)
