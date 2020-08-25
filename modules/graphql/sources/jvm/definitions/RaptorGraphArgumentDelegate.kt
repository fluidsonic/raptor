package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import kotlin.properties.*


public interface RaptorGraphArgumentDelegate<out Type> : PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, Type>> {

	@RaptorDsl
	public fun <TransformedType> map(
		transform: RaptorGraphInputScope.(value: Type) -> TransformedType,
	): RaptorGraphArgumentDelegate<TransformedType>

	@RaptorDsl
	public fun validate(
		validate: RaptorGraphInputScope.(value: Type) -> Unit,
	): RaptorGraphArgumentDelegate<Type>
}


@RaptorDsl
public fun <Value, TransformedValue> RaptorGraphArgumentDelegate<Maybe<Value>>.mapValue(
	transform: RaptorGraphInputScope.(value: Value) -> TransformedValue,
): RaptorGraphArgumentDelegate<Maybe<TransformedValue>> =
	map { maybe -> maybe.map { transform(it) } }


@RaptorDsl
public fun <Value : Any, TransformedValue> RaptorGraphArgumentDelegate<Maybe<Value?>>.mapValueIfNotNull(
	transform: RaptorGraphInputScope.(value: Value) -> TransformedValue?,
): RaptorGraphArgumentDelegate<Maybe<TransformedValue?>> =
	map { maybe -> maybe.mapIfNotNull { transform(it) } }


@RaptorDsl
public fun <Value : Any, NullableValue : Value?> RaptorGraphArgumentDelegate<NullableValue>.validateIfNotNull(
	validate: RaptorGraphInputScope.(value: Value) -> Unit,
): RaptorGraphArgumentDelegate<NullableValue> =
	validate { if (it != null) validate(it) }


@RaptorDsl
public fun <Value> RaptorGraphArgumentDelegate<Maybe<Value>>.validateValue(
	validate: RaptorGraphInputScope.(value: Value) -> Unit,
): RaptorGraphArgumentDelegate<Maybe<Value>> =
	validate { if (it.hasValue()) validate(it.get()) }


@RaptorDsl
public fun <Value : Any, NullableValue : Value?> RaptorGraphArgumentDelegate<Maybe<NullableValue>>.validateValueIfNotNull(
	validate: RaptorGraphInputScope.(value: Value) -> Unit,
): RaptorGraphArgumentDelegate<Maybe<NullableValue>> =
	validateValue { if (it != null) validate(it) }
