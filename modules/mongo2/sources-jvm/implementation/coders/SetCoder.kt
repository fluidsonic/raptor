package io.fluidsonic.raptor.mongo2

import kotlin.reflect.full.*
import org.bson.*


internal object SetCoder : MongoCoder<Set<Any?>> {

	override fun decodes(type: MongoValueType<in Set<Any?>>) =
		type.classifier == Set::class


	override fun encodes(type: MongoValueType<out Set<Any?>>) =
		type.classifier.isSubclassOf(Set::class)


	override fun MongoDecoderScope.decode(type: MongoValueType<in Set<Any?>>): Set<Any?> {
		val elementType = checkNotNull(type.arguments.singleOrNull()) { "Argument type missing." }
			as MongoValueType<Any>
		val elementDecoder = context.decoderRegistry.find(elementType)

		return with(elementDecoder) {
			buildSet {
				arrayByElement {
					add(
						when (bsonType()) {
							BsonType.NULL -> nullValue()
							else -> decode(elementType) // FIXME or null
						}
					)
				}
			}
		}
	}


	override fun MongoEncoderScope.encode(value: Set<Any?>, type: MongoValueType<out Set<Any?>>) {
		// FIXME
		val elementType = type.arguments.singleOrNull()?.takeIf { it.classifier != Any::class }

		@Suppress("UNCHECKED_CAST")
		val elementEncoder = elementType?.let { context.encoderRegistry.find(elementType) as MongoEncoder<Any> }

		array {
			when (elementEncoder) {
				null -> {
					for (element in value) {
						when (element) {
							null -> nullValue()
							else -> {
								val elementType = element::class.starProjectedType
								// FIXME
								//(context.encoderRegistry.find(elementType) as MongoEncoder<Any>).encode(writer, element, elementType, context)
							}
						}
					}
				}

				else -> with(elementEncoder) {
					for (element in value)
						when (element) {
							null -> nullValue() // FIXME native support
							else -> encode(element, elementType)
						}
				}
			}
		}
	}
}
