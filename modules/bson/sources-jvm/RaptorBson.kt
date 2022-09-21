package io.fluidsonic.raptor.bson

import io.fluidsonic.raptor.*
import org.bson.codecs.configuration.*


public interface RaptorBson {

	public val codecRegistry: CodecRegistry
	public val definitions: List<RaptorBsonDefinition>
	public val scope: RaptorBsonScope
}


internal object RaptorBsonKey : RaptorPropertyKey<RaptorBson> {

	override fun toString() = "bson"
}


public val RaptorContext.bson: RaptorBson
	get() = properties[RaptorBsonKey] ?: throw RaptorFeatureNotInstalledException(RaptorBsonFeature)
