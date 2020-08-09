package io.fluidsonic.raptor

import io.fluidsonic.raptor.quickstart.internal.*
import kotlin.reflect.*
import kotlin.reflect.full.*
import org.bson.*
import org.bson.types.*


interface EntityId {

	val factory: Factory<*>


	interface Factory<Id : EntityId> {

		val graphName: String

		val idClass: KClass<Id>

		val type: String

		fun parse(string: String): Id?

		fun parseWithoutType(string: String): Id?

		fun BsonReader.readIdValue(): Id

		fun BsonWriter.writeIdValue(id: Id)

		fun Id.serialize(): String

		fun Id.serializeWithoutType(): String
	}


	interface ObjectIdBased : EntityId { // FIXME prb. remove this, use String/Int, & convert to ObjectId only in codecs

		val value: ObjectId


		abstract class Factory<Id : ObjectIdBased>(
			final override val type: String, // FIXME confusing - rename - also, check for duplicates
			final override val idClass: KClass<Id>,
			final override val graphName: String = idClass.simpleName!!, // FIXME improve
			private val constructor: (raw: ObjectId) -> Id,
		) : EntityId.Factory<Id> {

			private val prefix = "$type/"


			final override fun parse(string: String) =
				string
					.takeIf { it.startsWith(prefix) || !it.contains('/') }
					?.removePrefix(prefix)
					?.let { parseWithoutType(it) }


			final override fun parseWithoutType(string: String) =
				string
					.let {
						try {
							ObjectId(it)
						}
						catch (_: Exception) {
							null
						}
					}
					?.let(constructor)


			final override fun BsonReader.readIdValue() =
				constructor(readObjectId())


			final override fun BsonWriter.writeIdValue(id: Id) =
				writeObjectId(id.value)


			final override fun Id.serialize() =
				prefix + serializeWithoutType()


			override fun Id.serializeWithoutType() =
				value.toHexString()!!
		}
	}


	interface StringBased : EntityId {

		val value: String


		abstract class Factory<Id : StringBased>(
			final override val type: String,
			final override val idClass: KClass<Id>,
			final override val graphName: String = idClass.simpleName!!, // FIXME improve
			private val constructor: (raw: String) -> Id,
		) : EntityId.Factory<Id> {

			private val prefix = "$type/"


			final override fun parse(string: String) =
				string
					.takeIf { it.startsWith(prefix) || !it.contains('/') }
					?.removePrefix(prefix)
					?.let { parseWithoutType(it) }


			final override fun parseWithoutType(string: String) =
				constructor(string)


			final override fun BsonReader.readIdValue() =
				constructor(readString())


			final override fun BsonWriter.writeIdValue(id: Id) =
				writeString(id.value)


			final override fun Id.serialize() =
				prefix + serializeWithoutType()


			override fun Id.serializeWithoutType() =
				value
		}
	}
}


fun <Id : EntityId> EntityId.Factory<Id>.bsonDefinition(): RaptorBsonDefinitions =
	EntityIdBsonDefinition(factory = this)


fun <Id : EntityId> EntityId.Factory<Id>.graphDefinition() = graphIdAliasDefinition<Id>(type = idClass.starProjectedType) {
	parse { parse(it) ?: error("\"$it\" is not a valid '$graphName'.") } // FIXME graph error
	serialize { it.serialize() }
}


@Suppress("UNCHECKED_CAST")
fun EntityId.toStringWithoutType() =
	(factory as EntityId.Factory<EntityId>).run { serializeWithoutType() }


val EntityId.typed
	get() = TypedId(this)


fun <Id : EntityId> EntityId.Factory<Id>.serialize(id: Id) =
	id.run { serialize() }
