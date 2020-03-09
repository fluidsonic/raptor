package io.fluidsonic.raptor


inline class TypedId(val untyped: EntityId) {

	override fun toString() =
		untyped.toString()


	companion object {

		internal fun bsonDefinition(factoryProvider: EntityIdFactoryProvider) = bsonDefinition<TypedId> {
			decode {
				readDocument {
					val factory = readString("type").let { type ->
						factoryProvider.idFactoryForType(type) ?: throw BsonException("ID type '$type' has not been registered with Baku")
					}

					readName("id")
					readValueOfType(factory.idClass).typed
				}
			}

			encode { value ->
				writeDocument {
					write("type", string = value.untyped.factory.type)
					write("id", value = value.untyped)
				}
			}
		}
	}
}
