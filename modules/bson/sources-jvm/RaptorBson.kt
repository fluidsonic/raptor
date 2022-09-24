package io.fluidsonic.raptor.bson

import org.bson.codecs.configuration.*


public interface RaptorBson {

	public val codecRegistry: CodecRegistry
	public val definitions: List<RaptorBsonDefinition>
	public val scope: RaptorBsonScope
}
