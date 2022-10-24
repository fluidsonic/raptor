//package io.fluidsonic.raptor
//
//import org.bson.types.*
//
//
//internal fun RaptorEntityId.Companion.graphDefinition() = graphIdAliasDefinition<RaptorEntityId> {
//	parse { TODO() }
//	serialize { TODO() }
//}
//
//
//public interface RaptorEntityIdObjectIdBased : RaptorEntityId {
//
//	public val value: ObjectId
//}
//
//
//public interface RaptorEntityIdStringBased : RaptorEntityId {
//
//	public val value: String
//}
//
//
//public interface RaptorEntityIdCompanion<Id : RaptorEntityId> {
//
//	public val definition: RaptorEntityIdDefinition<Id>
//
//
//	public fun Id.serialize(): String =
//		definition.serialize(this) // TODO add namespace
//}
//
//
//public fun <Id : RaptorEntityId> RaptorEntityIdCompanion<Id>.parseOrNull(string: String): Id? =
//	definition.parseOrNull(string)
