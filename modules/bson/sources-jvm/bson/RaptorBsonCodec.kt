package io.fluidsonic.raptor.bson

import kotlin.reflect.*


public interface RaptorBsonCodec<Value : Any> {

	public val valueClass: KClass<Value>

	public fun RaptorBsonReaderScope.decode(arguments: List<KTypeProjection>?): Value

	public fun RaptorBsonWriterScope.encode(value: Value)
}


/**
 * Implemented by a [RaptorBsonCodec] that wants precomputed [RaptorBsonType]s for its own generic type
 * arguments — e.g. `Foo<T>` wanting one for `T`, or `Bar<K, V>` wanting one for both — instead of resolving
 * them itself on every [decode] call via the reflective [KTypeProjection]-based path.
 *
 * The type-aware [decode] is used instead of the base [RaptorBsonCodec.decode] whenever the codec is reached
 * through a [RaptorBsonType] (i.e. via [RaptorBsonReader.valueOrThrow]/[RaptorBsonReader.valueOrNull]) — never
 * through the raw, reflective `value<T>()` path, which has no [RaptorBsonType] to offer. A codec implementing
 * this interface must therefore still implement the base `decode(arguments: List<KTypeProjection>?)` for that
 * path; the two may share an implementation or stay independent, but the base one has to work standalone.
 */
public interface RaptorBsonTypeAwareCodec<Value : Any> : RaptorBsonCodec<Value> {

	/**
	 * Decodes a value of the type [type] was resolved for.
	 *
	 * Read the type arguments through [RaptorBsonType.argumentTypes] — one precomputed [RaptorBsonType] per
	 * argument, `null` where a star projection leaves the argument's type statically unknown — and pick
	 * [RaptorBsonReader.valueOrNull] over [RaptorBsonReader.valueOrThrow] per argument according to
	 * [RaptorBsonType.argumentNullability].
	 *
	 * [type] is passed whole rather than just its argument types because per-argument nullability is
	 * data-shape information on the enclosing type, not on the argument types themselves — a [RaptorBsonType]
	 * is always for a non-null value.
	 */
	public fun RaptorBsonReaderScope.decode(type: RaptorBsonType<*>): Value
}


public fun <Value : Any> RaptorBsonCodec<Value>.decode(scope: RaptorBsonReaderScope, arguments: List<KTypeProjection>): Value =
	with(scope) {
		decode(arguments = arguments)
	}


public fun <Value : Any> RaptorBsonTypeAwareCodec<Value>.decode(scope: RaptorBsonReaderScope, type: RaptorBsonType<*>): Value =
	with(scope) {
		decode(type = type)
	}


public fun <Value : Any> RaptorBsonCodec<Value>.encode(scope: RaptorBsonWriterScope, value: Value) {
	with(scope) {
		encode(value)
	}
}
