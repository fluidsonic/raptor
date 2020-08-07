package io.fluidsonic.raptor

import io.fluidsonic.raptor.RaptorBsonDefinitions.*
import java.util.*
import org.bson.codecs.*
import org.bson.codecs.configuration.*


public class BsonRaptorComponent internal constructor() : RaptorComponent.Default<BsonRaptorComponent>() {

	internal val definitionsByPriority: MutableMap<Priority, MutableList<RaptorBsonDefinitions>> = EnumMap(Priority::class.java)
	internal var includesDefaultCodecs = false


	override fun RaptorComponentConfigurationEndScope.onConfigurationEnded() {
		// These codecs must come last to allow all other codecs to override default behavior.
		// Also, MongoDB would freak out with StackOverflowError if their own codecs don't come before these!
		val definitions =
			definitionsByPriority[Priority.high].orEmpty() +
				definitionsByPriority[Priority.normal].orEmpty() +
				definitionsByPriority[Priority.low].orEmpty() +
				if (includesDefaultCodecs) listOf(RaptorBsonDefaults.definitions) else emptyList()

		propertyRegistry.register(
			BsonConfiguration.PropertyKey,
			BsonConfiguration(definitions = RaptorBsonDefinitions.of(definitions))
		)
	}


	public companion object;


	internal object Key : RaptorComponentKey<BsonRaptorComponent> {

		override fun toString() = "bson"
	}
}


@RaptorDsl
public fun RaptorComponentSet<BsonRaptorComponent>.codecs(
	vararg codecs: Codec<*>,
	priority: Priority = Priority.normal,
) {
	codecs(codecs.asIterable(), priority = priority)
}


@RaptorDsl
public fun RaptorComponentSet<BsonRaptorComponent>.codecs(
	codecs: Iterable<Codec<*>>,
	priority: Priority = Priority.normal,
) {
	definitions(RaptorBsonDefinitions.of(codecs), priority = priority)
}


// FIXME can be confused with global bsonDefinition()
@RaptorDsl
public fun RaptorComponentSet<BsonRaptorComponent>.definitions(
	vararg definitions: RaptorBsonDefinitions,
	priority: Priority = Priority.normal,
) {
	definitions(definitions.asIterable(), priority = priority)
}


@RaptorDsl
public fun RaptorComponentSet<BsonRaptorComponent>.definitions(
	definitions: Iterable<RaptorBsonDefinitions>,
	priority: Priority = Priority.normal,
) {
	configure {
		definitionsByPriority.getOrPut(priority, ::mutableListOf).addAll(definitions)
	}
}


@RaptorDsl
public fun RaptorComponentSet<BsonRaptorComponent>.includeDefaultCodecs() {
	configure {
		if (includesDefaultCodecs)
			return@configure

		includesDefaultCodecs = true
	}
}


@RaptorDsl
public fun RaptorComponentSet<BsonRaptorComponent>.providers(
	vararg providers: CodecProvider,
	priority: Priority = Priority.normal,
) {
	providers(providers.asIterable(), priority = priority)
}


@RaptorDsl
public fun RaptorComponentSet<BsonRaptorComponent>.providers(
	providers: Iterable<CodecProvider>,
	priority: Priority = Priority.normal,
) {
	definitions(RaptorBsonDefinitions.of(providers), priority = priority)
}


@RaptorDsl
public fun RaptorComponentSet<BsonRaptorComponent>.registries(
	vararg registries: CodecRegistry,
	priority: Priority = Priority.normal,
) {
	registries(registries.asIterable(), priority = priority)
}


@RaptorDsl
public fun RaptorComponentSet<BsonRaptorComponent>.registries(
	registries: Iterable<CodecRegistry>,
	priority: Priority = Priority.normal,
) {
	definitions(RaptorBsonDefinitions.of(registries), priority = priority)
}
