package io.fluidsonic.raptor

import org.bson.types.*


public interface RaptorEntityId {

	public companion object BaseCompanion {

		internal fun graphDefinition() = graphIdAliasDefinition<RaptorEntityId> {
			parse { TODO() } // FIXME
			serialize { TODO() } // FIXME input only
		}
	}


	public interface ObjectIdBased : RaptorEntityId {

		public val value: ObjectId
	}


	public interface StringBased : RaptorEntityId {

		public val value: String
	}


	public interface Companion<Id : RaptorEntityId> {

		public val definition: RaptorEntityIdDefinition<Id>


		public fun Id.serialize(): String =
			definition.serialize(this)
	}
}


public fun <Id : RaptorEntityId> RaptorEntityId.Companion<Id>.parseOrNull(string: String): Id? =
	definition.parseOrNull(string)
