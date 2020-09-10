package io.fluidsonic.raptor.quickstart.internal

import io.fluidsonic.raptor.*
import kotlin.reflect.*


internal class EntityIdBsonDefinition<Id : EntityId>(
	val factory: EntityId.Factory<Id>,
) : RaptorBsonDefinition, RaptorBsonCodec<Id> {

	override val valueClass = factory.idClass


	@Suppress("UNCHECKED_CAST")
	override fun <Value : Any> codecForValueClass(valueClass: KClass<Value>, registry: RaptorBsonCodecRegistry): RaptorBsonCodec<Value>? =
		takeIf { valueClass == this.valueClass }
			?.let { this as RaptorBsonCodec<Value> }


	override fun RaptorBsonReaderScope.decode(): Id =
		with(factory) {
			reader.readIdValue()
		}


	override fun RaptorBsonWriterScope.encode(value: Id) {
		with(factory) {
			writer.writeIdValue(value)
		}
	}


	override fun toString() = "Raptor BSON definition (${valueClass.qualifiedName})"
}
