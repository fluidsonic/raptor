// FIXME support this through an ID factory[/definition] registry
//package io.fluidsonic.raptor
//
//import io.fluidsonic.raptor.quickstart.internal.*
//
//
//public inline class TypedId(public val untyped: EntityId) {
//
//	override fun toString(): String =
//		untyped.toString()
//
//
//	public companion object {
//
//		public fun bsonDefinition(): RaptorBsonDefinition = raptor.bson.definition<TypedId> {
//			val factoryByType: Map<String, EntityId.Factory<*>> = findEntityIdDefinitions(context.bsonConfiguration.definitions)
//				.map { it.factory }
//				.associateBy { it.type }
//
//			decode {
//				reader.document {
//					val factory = string("type").let { type ->
//						factoryByType[type] ?: error("No BSON definition provided for ID type '$type'.")
//					}
//
//					fieldName("id")
//					value(factory.idClass).typed
//				}
//			}
//
//			encode { value ->
//				writer.document {
//					value("type", value.untyped.factory.type)
//					value("id", value.untyped)
//				}
//			}
//		}
//	}
//}
//
//
//private fun findEntityIdDefinitions(
//	definitions: List<RaptorBsonDefinition>,
//	destination: MutableList<EntityIdBsonDefinition<*>> = mutableListOf(),
//): Collection<EntityIdBsonDefinition<*>> =
//	destination.apply {
//		for (definition in definitions) {
//			when (definition) {
//				is EntityIdBsonDefinition<*> -> add(definition)
//				else -> findEntityIdDefinitions(definitions = definition.additionalDefinitions, destination = this)
//			}
//		}
//	}
