package io.fluidsonic.raptor


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
