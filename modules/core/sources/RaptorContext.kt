package io.fluidsonic.raptor


interface RaptorContext : RaptorScope {

	val parent: RaptorContext?
	val properties: RaptorPropertySet

	override val context: RaptorContext
		get() = this

	override fun toString(): String


	companion object
}


operator fun <Value : Any> RaptorContext.get(key: RaptorPropertyKey<out Value>): Value? =
	properties[key]


val RaptorContext.root: RaptorContext
	get() = parent?.root ?: this
