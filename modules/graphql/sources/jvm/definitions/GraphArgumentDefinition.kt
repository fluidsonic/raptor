package io.fluidsonic.raptor

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.graphql.internal.*
import kotlin.properties.*
import kotlin.reflect.*


public class GraphArgumentDefinition<Value> internal constructor(
	internal val default: GValue?, // FIXME do not expose GValue - add value/obj builder
	name: String?,
	private val resolver: ArgumentResolver,
	internal val valueType: KType
) : PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, Value>> {

	private var isProvided = false

	internal var name = name // FIXME validate uniqueness
		private set


	public override operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadOnlyProperty<Any?, Value> {
		check(!isProvided) { "Cannot delegate multiple variables to the same argument." }

		val variableName = property.name
		val name = name ?: variableName

		this.isProvided = true
		this.name = name

		@Suppress("UNCHECKED_CAST")
		return ReadOnlyProperty { _, _ ->
			resolver.resolveArgument(name = name, variableName = variableName) as Value
		}
	}
}
