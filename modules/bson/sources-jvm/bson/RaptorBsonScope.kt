package io.fluidsonic.raptor.bson

import io.fluidsonic.raptor.*


public interface RaptorBsonScope : RaptorScope {

	@RaptorDsl
	public val codecRegistry: RaptorBsonCodecRegistry


	// FIXME
//	public val codecRegistry: CodecRegistry
//
//
//	public fun <Value : Any> BsonReader.readValueOfType(name: String, `class`: KClass<Value>): Value {
//		readName(name)
//
//		return readValueOfType(`class`)
//	}
//
//
//	// FIXME convert primitive to box classes
//	public fun <Value : Any> BsonReader.readValueOfType(`class`: KClass<Value>): Value =
//		codecRegistry[`class`.java].decode(this, decoderContext)
//
//
//	public fun <Value : Any> BsonReader.readValueOfTypeOrNull(name: String, `class`: KClass<Value>): Value? {
//		readName(name)
//
//		return readValueOfTypeOrNull(`class`)
//	}
//
//
//	public fun <Value : Any> BsonReader.readValueOfTypeOrNull(`class`: KClass<Value>): Value? {
//		expectValue("readValueOfTypeOrNull")
//
//		if (currentBsonType == BsonType.NULL) {
//			skipValue()
//
//			return null
//		}
//
//		return readValueOfType(`class`)
//	}
//
//
//	public fun <Value : Any> BsonReader.readValuesOfType(`class`: KClass<Value>): List<Value> =
//		readValuesOfType(`class`, container = mutableListOf())
//
//
//	public fun <Value, Container> BsonReader.readValuesOfType(
//		`class`: KClass<Value>,
//		container: Container,
//	): Container where Value : Any, Container : MutableCollection<Value> {
//		readArrayWithValues {
//			container.add(readValueOfType(`class`))
//		}
//
//		return container
//	}
//
//
//	public fun <Value : Any> BsonReader.readValuesOfTypeOrNull(`class`: KClass<Value>): List<Value>? {
//		expectValue("readValuesOfTypeOrNull")
//
//		if (currentBsonType == BsonType.NULL) {
//			skipValue()
//
//			return null
//		}
//
//		return readValuesOfType(`class`)
//	}
//
//
//	public fun <Value, Container> BsonReader.readValuesOfTypeOrNull(
//		`class`: KClass<Value>,
//		container: Container,
//	): Container? where Value : Any, Container : MutableCollection<Value> {
//		expectValue("readValuesOfTypeOrNull")
//
//		if (currentBsonType == BsonType.NULL) {
//			skipValue()
//
//			return null
//		}
//
//		return readValuesOfType(`class`, container = container)
//	}
//
//
//	// FIXME add all primitive overloads
//	@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
//	@LowPriorityInOverloadResolution
//	public fun BsonWriter.write(name: String, value: Any?, preserveNull: Boolean = false) {
//		if (value == null && !preserveNull)
//			return
//
//		writeName(name)
//		writeValue(value)
//	}
//
//
//	@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
//	@LowPriorityInOverloadResolution
//	public fun BsonWriter.write(name: String, values: Iterable<Any>?, preserveNull: Boolean = false) {
//		if (values == null && !preserveNull)
//			return
//
//		writeName(name)
//		writeValues(values)
//	}
//
//
//	public fun BsonWriter.writeValue(value: Any?) {
//		if (value == null)
//			return writeNull()
//
//		@Suppress("UNCHECKED_CAST")
//		(codecRegistry[value::class.java] as Encoder<Any>).encode(this, value, encoderContext)
//	}
//
//
//	public fun BsonWriter.writeValues(values: Iterable<Any>?) {
//		if (values == null)
//			return writeNull()
//
//		writeArray {
//			for (value in values)
//				writeValue(value)
//		}
//	}
//
//
//	public companion object {
//
//		internal val decoderContext = DecoderContext.builder().build()!!
//		internal val encoderContext = EncoderContext.builder().build()!!
//	}
}
