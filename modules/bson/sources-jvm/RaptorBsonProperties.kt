package io.fluidsonic.raptor

import org.bson.codecs.configuration.*


public interface RaptorBsonProperties {

	public val codecRegistry: CodecRegistry
	public val definitions: List<RaptorBsonDefinition>
	public val scope: RaptorBsonScope
}
