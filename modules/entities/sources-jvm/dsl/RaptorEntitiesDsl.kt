package io.fluidsonic.raptor

import kotlin.reflect.*
import org.bson.types.*


public object RaptorEntitiesDsl


// FIXME refactor all

@RaptorDsl
@Suppress("unused")
public val RaptorGlobalDsl.entities: RaptorEntitiesDsl
	get() = RaptorEntitiesDsl


@JvmName("idDefinitionObjectId")
@RaptorDsl
@Suppress("unused")
public inline fun <reified Id : RaptorEntityIdObjectIdBased> RaptorEntitiesDsl.idDefinition(
	discriminator: String,
	noinline factory: (objectId: ObjectId) -> Id,
): RaptorEntityIdDefinition<Id> =
	idDefinition(
		discriminator = discriminator,
		factory = factory,
		type = Id::class,
	)


@JvmName("idDefinitionObjectId")
@RaptorDsl
@Suppress("unused")
public fun <Id : RaptorEntityIdObjectIdBased> RaptorEntitiesDsl.idDefinition(
	discriminator: String,
	factory: (objectId: ObjectId) -> Id,
	type: KClass<Id>,
): RaptorEntityIdDefinition<Id> =
	RaptorEntityIdDefinition(
		bsonDefinition = raptor.bson.definition(type) {
			decode { factory(reader.objectId()) }
			encode { writer.value(it.value) }
		},
		discriminator = discriminator,
		stringCodec = RaptorEntityIdStringCodec.StringBased(
			parseOrNull = { string -> string.takeIf(ObjectId::isValid)?.let(::ObjectId)?.let(factory) },
			serialize = { id -> id.value.toHexString() }
		),
		type = type,
	)


@JvmName("idDefinitionString")
@RaptorDsl
@Suppress("unused")
public inline fun <reified Id : RaptorEntityIdStringBased> RaptorEntitiesDsl.idDefinition(
	discriminator: String,
	noinline factory: (string: String) -> Id,
): RaptorEntityIdDefinition<Id> =
	idDefinition(
		discriminator = discriminator,
		factory = factory,
		type = Id::class,
	)


@JvmName("idDefinitionString")
@RaptorDsl
@Suppress("unused")
public fun <Id : RaptorEntityIdStringBased> RaptorEntitiesDsl.idDefinition(
	discriminator: String,
	factory: (string: String) -> Id,
	type: KClass<Id>,
): RaptorEntityIdDefinition<Id> =
	RaptorEntityIdDefinition(
		bsonDefinition = raptor.bson.definition(type) {
			decode { factory(reader.string()) }
			encode { writer.value(it.value) }
		},
		discriminator = discriminator,
		stringCodec = RaptorEntityIdStringCodec.StringBased(
			parseOrNull = factory,
			serialize = { it.value }
		),
		type = type,
	)
