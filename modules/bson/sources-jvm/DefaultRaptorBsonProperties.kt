package io.fluidsonic.raptor

import io.fluidsonic.raptor.bson.internal.*
import org.bson.codecs.configuration.*


internal class DefaultRaptorBsonProperties(
	private val context: RaptorContext,
	override val definitions: List<RaptorBsonDefinition>,
) : RaptorBsonProperties {

	override val codecRegistry: CodecRegistry by lazy {
		scope.codecRegistry.internal()
	}


	override val scope: RaptorBsonScope by lazy {
		DefaultRaptorBsonScope(definitions = definitions, context = context)
	}


	internal object Key : RaptorPropertyKey<DefaultRaptorBsonProperties> {

		override fun toString() = "bson"
	}
}


public val RaptorContext.bson: RaptorBsonProperties
	get() = properties[DefaultRaptorBsonProperties.Key]
		?: error("You must install ${BsonRaptorFeature::class.simpleName} for enabling BSON functionality.")
