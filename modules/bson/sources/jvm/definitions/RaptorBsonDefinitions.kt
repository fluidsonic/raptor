package io.fluidsonic.raptor

import io.fluidsonic.stdlib.*
import org.bson.codecs.*
import org.bson.codecs.configuration.*


public interface RaptorBsonDefinitions {

	public val underlyingDefinitions: Collection<RaptorBsonDefinitions>
		get() = emptyList()


	public fun createCodecRegistry(scope: BsonScope): CodecRegistry


	public companion object {

		public val empty: RaptorBsonDefinitions
			get() = Empty


		public fun of(vararg codecs: Codec<*>): RaptorBsonDefinitions =
			of(codecs.toList())


		@JvmName("ofCodecs")
		public fun of(codecs: Iterable<Codec<*>>): RaptorBsonDefinitions =
			codecs
				.let { it as? List<Codec<*>> }
				.ifNull { codecs.toList() }
				.let { list ->
					when (list.size) {
						0 -> empty
						else -> of(CodecRegistries.fromCodecs(list))
					}
				}


		public fun of(vararg providers: CodecProvider): RaptorBsonDefinitions =
			of(providers.toList())


		@JvmName("ofProviders")
		public fun of(providers: Iterable<CodecProvider>): RaptorBsonDefinitions =
			providers
				.let { it as? List<CodecProvider> }
				.ifNull { providers.toList() }
				.let { list ->
					when (list.size) {
						0 -> empty
						else -> of(CodecRegistries.fromProviders(list))
					}
				}


		public fun of(registry: CodecRegistry): RaptorBsonDefinitions =
			OfRegistry(registry)


		public fun of(vararg registries: CodecRegistry): RaptorBsonDefinitions =
			of(registries.toList())


		@JvmName("ofRegistries")
		public fun of(registries: Iterable<CodecRegistry>): RaptorBsonDefinitions =
			registries
				.let { it as? List<CodecRegistry> }
				.ifNull { registries.toList() }
				.let { list ->
					when (list.size) {
						0 -> empty
						1 -> of(list[0])
						else -> of(CodecRegistries.fromRegistries(list))
					}
				}


		public fun of(vararg definitions: RaptorBsonDefinitions): RaptorBsonDefinitions =
			of(definitions.toList())


		@JvmName("ofDefinitions")
		public fun of(definitions: Iterable<RaptorBsonDefinitions>): RaptorBsonDefinitions =
			definitions
				.let { it as? List<RaptorBsonDefinitions> }
				.ifNull { definitions.toList() }
				.let { list ->
					when (list.size) {
						0 -> empty
						1 -> list[0]
						else -> OfDefinitions(list)
					}
				}
	}


	private object Empty : RaptorBsonDefinitions {

		override fun createCodecRegistry(scope: BsonScope) =
			EmptyRegistry


		override fun toString() =
			"<empty RaptorBsonDefinition>"
	}


	private object EmptyRegistry : CodecRegistry {

		override fun <T : Any?> get(clazz: Class<T>): Nothing? =
			null


		override fun <T : Any?> get(clazz: Class<T>, registry: CodecRegistry): Nothing? =
			null


		override fun toString() =
			"<empty CodecRegistry>"
	}


	private class OfDefinitions(
		override val underlyingDefinitions: Collection<RaptorBsonDefinitions>,
	) : RaptorBsonDefinitions {

		override fun createCodecRegistry(scope: BsonScope): CodecRegistry =
			CodecRegistries.fromRegistries(underlyingDefinitions.map { it.createCodecRegistry(scope) })


		override fun equals(other: Any?) =
			this === other || (other is OfDefinitions && underlyingDefinitions == other.underlyingDefinitions)


		override fun hashCode() =
			underlyingDefinitions.hashCode()


		override fun toString() =
			underlyingDefinitions.toString()
	}


	private class OfRegistry(
		private val registry: CodecRegistry,
	) : RaptorBsonDefinitions {

		override fun createCodecRegistry(scope: BsonScope) =
			registry


		override fun equals(other: Any?) =
			this === other || (other is OfRegistry && registry == other.registry)


		override fun hashCode() =
			registry.hashCode()


		override fun toString() =
			registry.toString()
	}


	public enum class Priority {

		high,
		normal,
		low
	}
}
