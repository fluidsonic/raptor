package io.fluidsonic.raptor.mongo2

import kotlin.reflect.*


// FIXME precalc hash code?
public data class MongoValueType<Value : Any>(
	val classifier: KClass<Value>,
	val arguments: List<MongoValueType<*>>,
) {

	init {
		check(classifier.typeParameters.size == arguments.size) {
			"Unexpected number of arguments (${arguments.size}) for '$classifier'. Expected ${classifier.typeParameters.size}."
		}
	}


	public companion object;


	override fun toString(): String =
		when {
			arguments.isEmpty() -> classifier.toString()
			else -> buildString {
				append(classifier)
				arguments.joinTo(this, prefix = "<", postfix = ">")
			}
		}
}


public fun <Value : Any> MongoValueType(classifier: KClass<Value>): MongoValueType<Value> =
	MongoValueType(classifier, arguments = classifier.typeParameters.map { MongoValueType(it) })


public fun MongoValueType(type: KType): MongoValueType<*> {
	val classifier = type.classifier as? KClass<*>
		?: error("Type '$type' doesn't have a KClass classifier.")

	val typeArguments = type.arguments
	val typeParameters = classifier.typeParameters
	check(typeParameters.size == typeArguments.size) { "Type '$type' has a different number of arguments than is expected for '$classifier'." }

	return MongoValueType(classifier, arguments = type.arguments.mapIndexed { index, argument ->
		MongoValueType(
			argument.type
				?: classifier.typeParameters[index].upperBounds.singleOrNull()
				?: error("Type '$type' uses a star projection for a type parameter with multiple upper bounds, which is not supported.")
		)
	})
}


public fun MongoValueType(type: KTypeParameter): MongoValueType<*> =
	MongoValueType(
		type.upperBounds.singleOrNull()
			?: error("Type parameters with multiple upper bounds are not supported.")
	)


public fun MongoValueType(type: KTypeProjection): MongoValueType<*> =
	MongoValueType(
		type.type
			?: error("Can't create MongoValueType for star projection without classifier. Use .of(fullType) or resolve it yourself.")
	)


@Suppress("UNCHECKED_CAST")
public inline fun <reified Value> MongoValueType(): MongoValueType<Value & Any> =
	MongoValueType(typeOf<Value>()) as MongoValueType<Value & Any>


public inline fun <reified Value : Any> MongoValueType(arguments: List<MongoValueType<*>>): MongoValueType<Value> =
	MongoValueType(Value::class, arguments)


public inline fun <reified Value : Any> MongoValueType(vararg arguments: MongoValueType<*>): MongoValueType<Value> =
	MongoValueType(arguments.toList())
