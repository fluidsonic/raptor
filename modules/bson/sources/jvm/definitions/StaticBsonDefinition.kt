package io.fluidsonic.raptor


// FIXME Simplify API. E.g. reusing bsonDefinition { } with special decodeFromString() and encodeAsString()
@RaptorDsl
inline fun <reified Value : Any> bsonDefinitionScoped(
	noinline parse: RaptorBsonDefinitionScope<Value>.(string: String) -> Value?, // FIXME don't allow null. use invalid() as raptor-graph
	noinline serialize: RaptorBsonDefinitionScope<Value>.(value: Value) -> String
) =
	bsonDefinition<Value> {
		decode {
			readString().let { string ->
				parse(string) ?: throw BsonException("Invalid value: $string")
			}
		}

		encode { value ->
			writeString(serialize(value))
		}
	}


@RaptorDsl
inline fun <reified Value : Any> bsonDefinition(
	noinline parse: (string: String) -> Value?,
	noinline serialize: (value: Value) -> String
) =
	bsonDefinition<Value> {
		decode {
			readString().let { string ->
				parse(string) ?: throw BsonException("Invalid value: $string")
			}
		}

		encode { value ->
			writeString(serialize(value))
		}
	}
