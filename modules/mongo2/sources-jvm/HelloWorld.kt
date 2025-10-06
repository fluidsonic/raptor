package foo

import io.fluidsonic.raptor.mongo2.*
import kotlinx.coroutines.*

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
//					StringCodec().asMongoDecoder(),
					SetCoder,
					SetHolderCoder,
				),
				encoder = MongoEncoderRegistry(
					TestDocumentCoder,
					TestDocument2Coder,
					SetCoder,
					SetHolderCoder,
				),
			)
		)
		.connectionString("mongodb://localhost:27017")
		.build()
	val database = client.database("raptor")

	val collection = database.collection<TestDocument>("test")

	collection.drop()
	println("Dropped collection.")

	collection.insertOne(TestDocument(id = "a", value = 2, optional = "optional"))
	println("Inserted value.")

	collection.insertOne(TestDocument(id = "b", value = 2, optional = null))
	println("Inserted value.")

	println("find()")
	collection.find().collect { value ->
		println("Found value: $value")
	}

	println("findAs()")
	collection.withValueType<TestDocument2>().find().collect { value ->
		println("Found value: $value")
	}

	println("findOneField(_id)")
	collection.findFieldValues<String?>("optional").collect { value ->
		println("Found value: $value")
	}

	val setCollection = database.collection<SetHolder<TestDocument>>("test")

	setCollection.drop()
	println("Dropped collection.")

	setCollection.insertOne(
		SetHolder(
			setOf(
				TestDocument(id = "a", value = 2, optional = "optional"),
				TestDocument(id = "b", value = 2, optional = "optional"),
			)
		)
	)
	println("Inserted value.")

	setCollection.insertOne(
		SetHolder(
			setOf(
				TestDocument(id = "c", value = 2, optional = null),
			)
		)
	)
	println("Inserted value.")

	println("find()")
	setCollection.find().collect { value ->
		println("Found value: $value")
	}

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


private object SetHolderCoder : MongoCoder<SetHolder<Any?>> {

	override fun decodes(type: MongoValueType<in SetHolder<Any?>>) =
		type.classifier == SetHolder::class


	override fun encodes(type: MongoValueType<out SetHolder<Any?>>) =
		type.classifier == SetHolder::class


	override fun MongoDecoderScope.decode(type: MongoValueType<in SetHolder<Any?>>): SetHolder<Any?> {
		val elementType = checkNotNull(type.arguments.singleOrNull()) { "Argument type missing." }
		val setType = MongoValueType<Set<*>>(elementType)

		val elementDecoder = context.decoderRegistry.find(setType) as MongoDecoder<Set<Any?>>

		var holder: SetHolder<Any?>? = null

		with(elementDecoder) {
			documentByField { name ->
				when (name) {
					"values" -> holder = SetHolder(decode(setType))
					else -> skipValue()
				}
			}

			return holder ?: error("values missing")
		}
	}


	override fun MongoEncoderScope.encode(value: SetHolder<Any?>, type: MongoValueType<out SetHolder<Any?>>) {
		val elementType = checkNotNull(type.arguments.singleOrNull()) { "Argument type missing." }
		val setType = MongoValueType<Set<*>>(elementType)

		val elementEncoder = context.encoderRegistry.find(setType) as MongoEncoder<Set<Any?>>

		with(elementEncoder) {
			document {
				fieldName("values")
				encode(value.values, setType)
			}
		}
	}
}
