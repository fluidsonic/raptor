package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*
import kotlin.properties.*


public interface RaptorGraphArgumentDelegate<out Type> : PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, Type>> {

	@RaptorDsl
	public fun <TransformedType> map(
		// TODO Rename to transform?
		transform: RaptorGraphInputScope.(value: Type) -> TransformedType,
	): RaptorGraphArgumentDelegate<TransformedType>
}


@RaptorDsl
public fun <Type> RaptorGraphArgumentDelegate<Type>.validate(
	validate: RaptorGraphInputScope.(value: Type) -> Unit,
): RaptorGraphArgumentDelegate<Type> =
	map { value -> value.also { validate(it) } }


@RaptorDsl
public fun <Type : Any, NullableType : Type?> RaptorGraphArgumentDelegate<NullableType>.validateIfNotNull(
	validate: RaptorGraphInputScope.(value: Type) -> Unit,
): RaptorGraphArgumentDelegate<NullableType> =
	validate { if (it != null) validate(it) }
