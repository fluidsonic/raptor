package bar

import io.fluidsonic.raptor.keyvaluestore.*
import io.fluidsonic.raptor.keyvaluestore.mongo2.*
import io.fluidsonic.raptor.mongo2.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.bson.codecs.*

public fun main(vararg args: String) {
	runBlocking { test() }
}

private suspend fun test() {
	val client = MutableMongoClient.default()
		.coderRegistry(
			MongoCoderRegistry(
				decoder = MongoDecoderRegistry(
					TestDocumentCoder,
					TestDocument2Coder,
//					BsonDocumentCodec().asMongoDecoder(),
					StringCodec().asMongoDecoder(),
				),
				encoder = MongoEncoderRegistry(
					TestDocumentCoder,
					TestDocument2Coder,
				),
			)
		)
		.connectionString("mongodb://localhost:27017")
		.build()
	val database = client.database("raptor")

	val kvs = RaptorKeyValueStoreFactory.mongo2(database).create<Int, Set<String?>>("test-kvs")
	kvs.clear()

	kvs.set(1, setOf("1", "2"))
	kvs.setIfAbsent(1, setOf("3", "4"))
	kvs.set(1, setOf("1", "2", "3"))
	kvs.setIfAbsent(2, setOf("1", null, "2", "3"))
	kvs.setIfAbsent(3, emptySet())
	kvs.setIfAbsent(4, emptySet())
	kvs.setIfAbsent(5, setOf("xx"))
	kvs.setIfAbsent(6, setOf("xx2"))
	kvs.setIfAbsentOrRemove(4, null)
	kvs.setOrRemove(5, null)
	kvs.setOrRemove(5, null)
	kvs.remove(6)
	println("Keys: " + kvs.keys().toList().joinToString())
	println("Values: " + kvs.values().toList().joinToString())
	println("Entries: " + kvs.entries().toList().joinToString())
	println("1: " + kvs.get(1))

	println("fin")
}


private data class SetHolder<Value>(
	val values: Set<Value>,
)


private data class TestDocument(
	val id: String,
	val value: Int,
	val optional: String?,
)


private data class TestDocument2(
	val id: String,
	val value: Int,
	val optional: String?,
)


private object TestDocumentCoder : MongoCoder<TestDocument> {

	override fun decodes(type: MongoValueType<in TestDocument>) =
		type.classifier == TestDocument::class


	override fun encodes(type: MongoValueType<out TestDocument>) =
		type.classifier == TestDocument::class


	override fun MongoDecoderScope.decode(type: MongoValueType<in TestDocument>): TestDocument =
		document {
			val id = string("_id")
			val value = int("value")
			val optional = stringOrNull("optional")

			TestDocument(id, value, optional)
		}


	override fun MongoEncoderScope.encode(value: TestDocument, type: MongoValueType<out TestDocument>) {
		document {
			string("_id", value.id)
			int("value", value.value)
			stringOrNull("optional", value.optional, preserveNull = true)
		}
	}
}


private object TestDocument2Coder : MongoCoder<TestDocument2> {

	override fun decodes(type: MongoValueType<in TestDocument2>) =
		type.classifier == TestDocument2::class


	override fun encodes(type: MongoValueType<out TestDocument2>) =
		type.classifier == TestDocument2::class


	override fun MongoDecoderScope.decode(type: MongoValueType<in TestDocument2>): TestDocument2 =
		document {
			val id = string("_id")
			val value = int("value")
			val optional = stringOrNull("optional")

			TestDocument2(id, value, optional)
		}


	override fun MongoEncoderScope.encode(value: TestDocument2, type: MongoValueType<out TestDocument2>) {
		document {
			string("_id", value.id)
			int("value", value.value)
			stringOrNull("optional", value.optional, preserveNull = true)
		}
	}
}
