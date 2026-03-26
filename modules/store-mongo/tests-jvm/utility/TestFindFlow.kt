package tests.utility

import com.mongodb.*
import com.mongodb.client.model.*
import io.fluidsonic.mongo.*
import kotlinx.coroutines.flow.*
import org.bson.*
import org.bson.codecs.*
import org.bson.codecs.configuration.*
import org.bson.conversions.*
import java.util.concurrent.*
import kotlin.reflect.*


internal class TestFindFlow<TResult : Any>(
	private val documents: List<BsonDocument>,
	private val codecRegistry: CodecRegistry,
	private val resultClass: KClass<out TResult>,
	private val filterBson: Bson? = null,
	private val projectionBson: Bson? = null,
) : FindFlow<TResult> {

	private fun filteredDocuments(): List<BsonDocument> {
		if (filterBson == null) return documents

		val filterDoc = filterBson.toBsonDocument(BsonDocument::class.java, codecRegistry)
		val idValue = filterDoc["_id"] ?: return documents

		return documents.filter { doc -> doc["_id"] == idValue }
	}

	private fun projectedDocuments(): List<BsonDocument> {
		val docs = filteredDocuments()
		if (projectionBson == null) return docs

		val projDoc = projectionBson.toBsonDocument(BsonDocument::class.java, codecRegistry)
		val includeFields = mutableSetOf<String>()
		var excludeId = false

		for (key in projDoc.keys) {
			val value = projDoc[key]
			if (value is BsonInt32) {
				if (value.value == 1) includeFields.add(key)
				else if (value.value == 0 && key == "_id") excludeId = true
			} else if (value is BsonDocument) {
				// Nested projection document (from Projections.fields combining multiple projections)
				for (nestedKey in value.keys) {
					val nestedValue = value[nestedKey]
					if (nestedValue is BsonInt32) {
						if (nestedValue.value == 1) includeFields.add(nestedKey)
						else if (nestedValue.value == 0 && nestedKey == "_id") excludeId = true
					}
				}
			}
		}

		return docs.map { doc ->
			val result = BsonDocument()
			for (field in includeFields) {
				val value = doc[field]
				if (value != null) result.append(field, value)
			}
			if (!excludeId && !includeFields.contains("_id")) {
				val idValue = doc["_id"]
				if (idValue != null) result.append("_id", idValue)
			}
			result
		}
	}

	@Suppress("UNCHECKED_CAST")
	private fun decodeDocument(doc: BsonDocument): TResult {
		if (resultClass == RawBsonDocument::class) {
			val rawDoc = RawBsonDocument.parse(doc.toJson())
			return rawDoc as TResult
		}

		val codec = codecRegistry.get(resultClass.java) as Codec<TResult>
		val reader = BsonDocumentReader(doc)
		return codec.decode(reader, DecoderContext.builder().build())
	}

	override suspend fun collect(collector: FlowCollector<TResult>) {
		for (doc in projectedDocuments()) {
			collector.emit(decodeDocument(doc))
		}
	}

	override fun filter(filter: Bson?): FindFlow<TResult> =
		TestFindFlow(
			documents = documents,
			codecRegistry = codecRegistry,
			resultClass = resultClass,
			filterBson = filter,
			projectionBson = projectionBson,
		)

	override fun projection(projection: Bson?): FindFlow<TResult> =
		TestFindFlow(
			documents = documents,
			codecRegistry = codecRegistry,
			resultClass = resultClass,
			filterBson = filterBson,
			projectionBson = projection,
		)

	override suspend fun firstOrNull(): TResult? {
		val docs = projectedDocuments()
		return docs.firstOrNull()?.let { decodeDocument(it) }
	}

	override fun limit(limit: Int): FindFlow<TResult> = this
	override fun skip(skip: Int): FindFlow<TResult> = this
	override fun maxTime(maxTime: Long, timeUnit: TimeUnit): FindFlow<TResult> = this
	override fun maxAwaitTime(maxAwaitTime: Long, timeUnit: TimeUnit): FindFlow<TResult> = this
	override fun sort(sort: Bson?): FindFlow<TResult> = this
	override fun noCursorTimeout(noCursorTimeout: Boolean): FindFlow<TResult> = this
	override fun oplogReplay(oplogReplay: Boolean): FindFlow<TResult> = this
	override fun partial(partial: Boolean): FindFlow<TResult> = this
	override fun cursorType(cursorType: CursorType): FindFlow<TResult> = this
	override fun collation(collation: Collation?): FindFlow<TResult> = this
	override fun comment(comment: String?): FindFlow<TResult> = this
	override fun hint(hint: Bson?): FindFlow<TResult> = this
	override fun max(max: Bson?): FindFlow<TResult> = this
	override fun min(min: Bson?): FindFlow<TResult> = this
	override fun returnKey(returnKey: Boolean): FindFlow<TResult> = this
	override fun showRecordId(showRecordId: Boolean): FindFlow<TResult> = this
	override fun batchSize(batchSize: Int): FindFlow<TResult> = this
}
