package io.fluidsonic.raptor


interface RaptorContext {

	val parent: RaptorContext?
	val properties: RaptorPropertySet

	fun asScope(): RaptorScope
	override fun toString(): String


	companion object
}


operator fun <Value : Any> RaptorContext.get(key: RaptorPropertyKey<out Value>): Value? =
	properties[key]


val RaptorContext.root: RaptorContext
	get() = parent?.root ?: this
