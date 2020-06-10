package io.fluidsonic.raptor


interface RaptorContext : RaptorScope {

	val parent: RaptorContext?
	val properties: RaptorPropertySet


	override fun toString(): String


	fun asScope(): RaptorScope =
		this


	override val context: RaptorContext
		get() = this


	companion object


	interface Lazy : RaptorContext
}


operator fun <Value : Any> RaptorContext.get(key: RaptorPropertyKey<out Value>): Value? =
	properties[key]


val RaptorContext.root: RaptorContext
	get() = parent?.root ?: this
