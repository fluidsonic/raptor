package io.fluidsonic.raptor


internal interface RaptorEntityIdStringCodec<Id : RaptorEntityId> {

	fun parseOrNull(string: String): Id?
	fun serialize(id: Id): String


	class StringBased<Id : RaptorEntityId>(
		private val parseOrNull: (string: String) -> Id?,
		private val serialize: (id: Id) -> String,
	) : RaptorEntityIdStringCodec<Id> {

		override fun parseOrNull(string: String): Id? =
			parseOrNull.invoke(string)


		override fun serialize(id: Id): String =
			serialize.invoke(id)
	}
}
