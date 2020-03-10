package io.fluidsonic.raptor

import kotlin.reflect.*


private class ScopedBsonDefinition<Value : Any>(
	private val config: RaptorBsonDefinitionScope<Value>.() -> Unit,
	private val valueClass: KClass<Value>
) : RaptorBsonDefinition<Value> {

	override fun codec(scope: BsonScope) =
		BsonDefinitionScopeImpl(scope = scope, valueClass = valueClass).apply(config).codec()
}


inline fun <reified Value : Any> bsonDefinition(@BuilderInference noinline config: RaptorBsonDefinitionScope<Value>.() -> Unit) =
	bsonDefinition(valueClass = Value::class, config = config)


fun <Value : Any> bsonDefinition(valueClass: KClass<Value>, config: RaptorBsonDefinitionScope<Value>.() -> Unit): RaptorBsonDefinition<Value> =
	ScopedBsonDefinition(config = config, valueClass = valueClass)
