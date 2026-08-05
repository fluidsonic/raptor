package io.fluidsonic.raptor.bson

import kotlin.reflect.*


/**
 * A reusable, pre-resolved token for one non-null BSON value type, created by [type][RaptorBsonDsl.type].
 *
 * Resolve a type once — typically into a `val` outside any `decode { … }` block — and read the corresponding
 * field through [RaptorBsonReader.valueOrThrow] or [RaptorBsonReader.valueOrNull], depending on whether that
 * field may be BSON null. Doing so skips the type dispatch and the codec lookup that
 * `reader.value<Value>(field)` otherwise repeats on every single read of that field.
 *
 * Not to be confused with `org.bson.BsonType`, the wire-format type enum describing what a field currently
 * holds — see [RaptorBsonReader.bsonType].
 *
 * A [RaptorBsonType] is always for a non-null [Value] — nullability is a call-site choice (`valueOrThrow` vs
 * `valueOrNull` on read, or the value argument on write), never a property of the type token itself, so
 * `raptor.bson.type<Country?>()` cannot be constructed at all.
 *
 * The codec is bound lazily on first use rather than at construction, because these tokens are normally
 * created while Raptor is still being assembled — before any [RaptorBsonCodecRegistry] exists. The binding is
 * cached per registry, so a [RaptorBsonType] may safely be shared between registries and between threads.
 */
public class RaptorBsonType<Value : Any> internal constructor(
	internal val type: KType,
) {

	internal val arguments: List<KTypeProjection> = type.arguments
	internal val collectionKind: BsonCollectionKind? = bsonCollectionKindOf(type.classifier)

	/**
	 * One [RaptorBsonType] per type argument of this token's own type, resolved once here at construction —
	 * what this class already does for a `List`/`Set` element type, generalized to any generic type with any
	 * number of arguments.
	 *
	 * An entry is `null` where argument `i` is a star projection (`Foo<*>`) whose type is not statically known
	 * and therefore cannot be turned into a [RaptorBsonType].
	 *
	 * Consumed by a [RaptorBsonTypeAwareCodec] to read its own type arguments without re-resolving them per
	 * decode.
	 */
	public val argumentTypes: List<RaptorBsonType<Any>?> = type.arguments.map { projection ->
		projection.type?.let { RaptorBsonType<Any>(it) }
	}

	/**
	 * Whether each type argument may be BSON null, parallel to [argumentTypes] — e.g. `Foo<Bar?, Baz>` is
	 * `[true, false]`.
	 *
	 * This is data-shape information carried on the enclosing type rather than a call-site choice, so it lives
	 * here and not on the argument types: a [RaptorBsonType] is always for a non-null value.
	 * A star-projected argument counts as non-null.
	 */
	public val argumentNullability: List<Boolean> = type.arguments.map { it.type?.isMarkedNullable ?: false }

	// Whether this collection's *elements* may be BSON null — e.g. `List<Country?>`. This is data-shape
	// information carried on the collection type itself, not a call-site choice, so `collectionValue` reads
	// it off the enclosing type instead of needing a nullable-typed element type, which the `Value : Any` bound
	// above rules out entirely.
	internal val elementIsNullable: Boolean = collectionKind
		?.let { type.arguments.single().type?.isMarkedNullable }
		?: false

	// Only present for a collection type, and only when its element type is known (a star projection leaves
	// it unknown). Precomputing a full `RaptorBsonType` rather than a bare `KType` means the element's codec is
	// resolved once for the whole collection type too, not once per element read. The element `KType` may
	// itself be marked nullable (e.g. `List<Country?>`) — that marking is irrelevant here and ignored, since
	// neither `codec(registry)` nor `collectionKind` consult it; [elementIsNullable] is what actually carries
	// it.
	internal val elementType: RaptorBsonType<Any>? = collectionKind
		?.let { type.arguments.single().type }
		?.let { RaptorBsonType<Any>(it) }

	@Volatile
	private var binding: Binding? = null


	// A single volatile reference to an immutable pair - rather than two independently volatile fields - so
	// that a concurrent rebinding can never be observed as one binding's registry beside another's codec.
	internal fun codec(registry: RaptorBsonCodecRegistry): RaptorBsonCodec<Any> {
		binding?.let { binding ->
			if (binding.registry === registry)
				return binding.codec
		}

		@Suppress("UNCHECKED_CAST")
		val valueClass = type.classifier as? KClass<Any>
			?: error("Cannot decode type '$type'.")

		val codec = registry[valueClass]
		binding = Binding(codec = codec, registry = registry)

		return codec
	}


	override fun toString(): String =
		"BSON type for '$type'"


	private class Binding(
		val codec: RaptorBsonCodec<Any>,
		val registry: RaptorBsonCodecRegistry,
	)
}
