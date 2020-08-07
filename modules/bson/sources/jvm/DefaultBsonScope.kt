package io.fluidsonic.raptor


internal class DefaultBsonScope(
	configuration: BsonConfiguration,
	context: RaptorContext,
) : BsonScope, RaptorScope by context {

	override val codecRegistry = configuration.definitions.createCodecRegistry(scope = this)
}
