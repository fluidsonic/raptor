package io.fluidsonic.raptor.mongo2

import org.bson.conversions.*


public interface MutableMongoCollection<Value : Any> : MongoCollection<Value> {

	public suspend fun deleteMany(filter: Bson, options: MongoDeleteOptions = MongoDeleteOptions.empty): MongoDeleteResult?
	public suspend fun deleteOne(filter: Bson, options: MongoDeleteOptions = MongoDeleteOptions.empty): MongoDeleteResult?
	public suspend fun drop()
	public suspend fun insertOne(value: Value, options: MongoInsertOneOptions = MongoInsertOneOptions.empty): MongoInsertOneResult?
	public suspend fun insertMany(values: List<Value>, options: MongoInsertManyOptions = MongoInsertManyOptions.empty): MongoInsertManyResult?
	public suspend fun replaceOne(filter: Bson, replacement: Value, options: MongoReplaceOptions = MongoReplaceOptions.empty): MongoUpdateResult?
	public suspend fun updateMany(filter: Bson, update: Bson, options: MongoUpdateOptions = MongoUpdateOptions.empty): MongoUpdateResult?
	public suspend fun updateOne(filter: Bson, update: Bson, options: MongoUpdateOptions = MongoUpdateOptions.empty): MongoUpdateResult?
	public override fun withCoderRegistry(coderRegistry: MongoCoderRegistry): MutableMongoCollection<Value>
	public override fun <NewValue : Any> withValueType(valueType: MongoValueType<NewValue>): MutableMongoCollection<NewValue>


	public suspend fun deleteAll(options: MongoDeleteOptions = MongoDeleteOptions.empty): MongoDeleteResult? =
		deleteMany(MongoFilters.empty(), options)


	public suspend fun deleteOneById(id: Any?, options: MongoDeleteOptions = MongoDeleteOptions.empty): MongoDeleteResult? =
		deleteOne(MongoFilters.eq(id), options)


	public suspend fun replaceOneById(id: Any?, replacement: Value, options: MongoReplaceOptions = MongoReplaceOptions.empty): MongoUpdateResult? =
		replaceOne(MongoFilters.eq(id), replacement, options)


	public suspend fun updateAll(update: Bson, options: MongoUpdateOptions = MongoUpdateOptions.empty): MongoUpdateResult? =
		updateMany(filter = MongoFilters.empty(), update = update, options)


	public suspend fun updateOneById(id: Any?, update: Bson, options: MongoUpdateOptions = MongoUpdateOptions.empty): MongoUpdateResult? =
		updateOne(MongoFilters.eq(id), update, options)


	public companion object
}


public inline fun <reified Value : Any> MutableMongoCollection<*>.withValueType(): MutableMongoCollection<Value> =
	withValueType(MongoValueType<Value>())
