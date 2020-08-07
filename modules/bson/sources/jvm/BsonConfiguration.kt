package io.fluidsonic.raptor


public class BsonConfiguration(
	public val definitions: RaptorBsonDefinitions,
) {

	public companion object {

		public val empty: BsonConfiguration = BsonConfiguration(definitions = RaptorBsonDefinitions.empty)
	}


	internal object PropertyKey : RaptorPropertyKey<BsonConfiguration> {

		override fun toString() = "bson"
	}
}
