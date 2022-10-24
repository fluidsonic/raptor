package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import kotlin.properties.*


public interface RaptorGraphArgumentDelegate<out Type> : PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, Type>> {

	@RaptorDsl
	public fun <TransformedType> map(
		// TODO rn to transform?
		transform: RaptorGraphInputScope.(value: Type) -> TransformedType,
	): RaptorGraphArgumentDelegate<TransformedType>
}


@RaptorDsl
public fun <Type, TransformedType> RaptorGraphArgumentDelegate<Maybe<Type>>.mapValue(
	transform: RaptorGraphInputScope.(value: Type) -> TransformedType,
): RaptorGraphArgumentDelegate<Maybe<TransformedType>> =
	map { maybe -> maybe.map { transform(it) } }


@RaptorDsl
public fun <Type : Any, TransformedType> RaptorGraphArgumentDelegate<Maybe<Type?>>.mapValueIfNotNull(
	transform: RaptorGraphInputScope.(value: Type) -> TransformedType?,
): RaptorGraphArgumentDelegate<Maybe<TransformedType?>> =
	map { maybe -> maybe.mapIfNotNull { transform(it) } }


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


@RaptorDsl
public fun <Type> RaptorGraphArgumentDelegate<Maybe<Type>>.validateValue(
	validate: RaptorGraphInputScope.(value: Type) -> Unit,
): RaptorGraphArgumentDelegate<Maybe<Type>> =
	validate { if (it.hasValue()) validate(it.get()) }


@RaptorDsl
public fun <Type : Any, NullableType : Type?> RaptorGraphArgumentDelegate<Maybe<NullableType>>.validateValueIfNotNull(
	validate: RaptorGraphInputScope.(value: Type) -> Unit,
): RaptorGraphArgumentDelegate<Maybe<NullableType>> =
	validateValue { if (it != null) validate(it) }
