package io.fluidsonic.raptor.di


public interface RaptorDIKey<Value> {

	public val isOptional: Boolean

	public fun notOptional(): RaptorDIKey<Value>
}
