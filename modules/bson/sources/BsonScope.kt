package io.fluidsonic.raptor

import org.bson.*
import org.bson.codecs.*
import org.bson.codecs.configuration.*
import kotlin.reflect.*


interface BsonScope : RaptorScope {

	val codecRegistry: CodecRegistry


	fun <Value : Any> BsonReader.readValueOfType(name: String, `class`: KClass<Value>): Value {
		readName(name)

		return readValueOfType(`class`)
	}


	// FIXME convert primitive to box classes
	fun <Value : Any> BsonReader.readValueOfType(`class`: KClass<Value>): Value =
		codecRegistry[`class`.java].decode(this, decoderContext)


	fun <Value : Any> BsonReader.readValueOfTypeOrNull(name: String, `class`: KClass<Value>): Value? {
		readName(name)

		return readValueOfTypeOrNull(`class`)
	}


	fun <Value : Any> BsonReader.readValueOfTypeOrNull(`class`: KClass<Value>): Value? {
		expectValue("readValueOfTypeOrNull")

		if (currentBsonType == BsonType.NULL) {
			skipValue()

			return null
		}

		return readValueOfType(`class`)
	}


	fun <Value : Any> BsonReader.readValuesOfType(`class`: KClass<Value>): List<Value> =
		readValuesOfType(`class`, container = mutableListOf())


	fun <Value, Container> BsonReader.readValuesOfType(`class`: KClass<Value>, container: Container): Container where Value : Any, Container : MutableCollection<Value> {
		readArrayWithValues {
			container.add(readValueOfType(`class`))
		}

		return container
	}


	fun <Value : Any> BsonReader.readValuesOfTypeOrNull(`class`: KClass<Value>): List<Value>? {
		expectValue("readValuesOfTypeOrNull")

		if (currentBsonType == BsonType.NULL) {
			skipValue()

			return null
		}

		return readValuesOfType(`class`)
	}


	fun <Value, Container> BsonReader.readValuesOfTypeOrNull(`class`: KClass<Value>, container: Container): Container? where Value : Any, Container : MutableCollection<Value> {
		expectValue("readValuesOfTypeOrNull")

		if (currentBsonType == BsonType.NULL) {
			skipValue()

			return null
		}

		return readValuesOfType(`class`, container = container)
	}


	fun BsonWriter.write(name: String, value: Any?, preserveNull: Boolean = false) {
		if (value == null && !preserveNull)
			return

		writeName(name)
		writeValue(value)
	}


	fun BsonWriter.write(name: String, values: Iterable<Any>?, preserveNull: Boolean = false) { // FIXME use preserveNull in BsonWriter extensions
		if (values == null && !preserveNull)
			return

		writeName(name)
		writeValues(values)
	}


	fun BsonWriter.writeValue(value: Any?) {
		if (value == null)
			return writeNull()

		@Suppress("UNCHECKED_CAST")
		(codecRegistry[value::class.java] as Encoder<Any>).encode(this, value, encoderContext)
	}


	fun BsonWriter.writeValues(values: Iterable<Any>?) {
		if (values == null)
			return writeNull()

		writeArray {
			for (value in values)
				writeValue(value)
		}
	}


	companion object {

		internal val decoderContext = DecoderContext.builder().build()!!
		internal val encoderContext = EncoderContext.builder().build()!!
	}
}
