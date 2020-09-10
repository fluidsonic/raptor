package io.fluidsonic.raptor


public class RaptorBsonConfiguration internal constructor(
	public val definitions: List<RaptorBsonDefinition>,
) {

	public companion object {

		public val empty: RaptorBsonConfiguration = RaptorBsonConfiguration(definitions = emptyList())
	}


	internal object PropertyKey : RaptorPropertyKey<RaptorBsonConfiguration> {

		override fun toString() = "bson"
	}
}
