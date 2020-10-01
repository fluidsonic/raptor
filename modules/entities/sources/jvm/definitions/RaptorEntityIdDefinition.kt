package io.fluidsonic.raptor

import kotlin.reflect.*
import kotlin.reflect.full.*


public class RaptorEntityIdDefinition<Id : RaptorEntityId> internal constructor(
	internal val bsonDefinition: RaptorBsonDefinition,
	public val discriminator: String,
	private val stringCodec: RaptorEntityIdStringCodec<Id>,
	public val type: KClass<Id>,
) {

	internal val resolverType = RaptorEntityResolver::class.createType(listOf(
		KTypeProjection.contravariant(type.starProjectedType),
		KTypeProjection.STAR
	))


	internal fun graphDefinition() = graphIdAliasDefinition<Id>(type = type.starProjectedType) {
		parse { stringCodec.parseOrNull(it) ?: invalid("\"$it\" is not a valid '$discriminator' ID.") }
		serialize { stringCodec.serialize(it) }
	}


	public fun parseOrNull(string: String): Id? =
		stringCodec.parseOrNull(string)


	public fun serialize(id: Id): String =
		stringCodec.serialize(id)
}
