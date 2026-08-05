package io.fluidsonic.raptor.bson

import kotlin.reflect.*


// Computed once (class init) rather than re-resolved on every value read. `List`/`MutableList` and
// `Collection`/`MutableCollection` erase to the same Java class on the JVM (Kotlin's read-only/mutable view
// is compile-time only), and likewise `Set`/`MutableSet` - so each pair collapses to one erased-class
// constant here, which is also why the original nine-`KClass`-literal `when` had three dead duplicate
// branches (`KClassImpl.equals` is defined as `javaObjectType == other.javaObjectType`).
private val arrayListErasedClass = ArrayList::class.javaObjectType
private val listErasedClass = List::class.javaObjectType
private val collectionErasedClass = Collection::class.javaObjectType
private val hashSetErasedClass = HashSet::class.javaObjectType
private val linkedHashSetErasedClass = LinkedHashSet::class.javaObjectType
private val setErasedClass = Set::class.javaObjectType


// The collection shape a value type decodes into. `null` from [bsonCollectionKindOf] means the reader does
// not build the collection itself and the value has to be decoded through a codec from the registry instead.
internal enum class BsonCollectionKind {

	list,
	set
}


internal fun bsonCollectionKindOf(classifier: KClassifier?): BsonCollectionKind? =
	when ((classifier as? KClass<*>)?.javaObjectType) {
		arrayListErasedClass, listErasedClass, collectionErasedClass -> BsonCollectionKind.list
		hashSetErasedClass, linkedHashSetErasedClass, setErasedClass -> BsonCollectionKind.set
		else -> null
	}
