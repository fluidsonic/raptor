package io.fluidsonic.raptor.mongo2


internal data class ImmutableMongoCollection<Value : Any>(
	private val source: MongoCollection<Value>,
) : MongoCollection<Value> by source {

	override fun asImmutable(): MongoCollection<Value> =
		this


	override fun withCoderRegistry(coderRegistry: MongoCoderRegistry): MongoCollection<Value> =
		source.withCoderRegistry(coderRegistry).asImmutable()


	override fun <NewValue : Any> withValueType(valueType: MongoValueType<NewValue>): MongoCollection<NewValue> =
		source.withValueType(valueType).asImmutable()
}
