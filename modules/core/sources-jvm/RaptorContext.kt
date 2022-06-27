package io.fluidsonic.raptor


public interface RaptorContext : RaptorScope {

	public val parent: RaptorContext?
	public val properties: RaptorPropertySet


	override fun toString(): String


	public fun asScope(): RaptorScope =
		this


	override val context: RaptorContext
		get() = this


	public companion object;


	public interface Lazy : RaptorContext
}


public operator fun <Value : Any> RaptorContext.get(key: RaptorPropertyKey<out Value>): Value? =
	properties[key]


public val RaptorContext.root: RaptorContext
	get() = parent?.root ?: this
