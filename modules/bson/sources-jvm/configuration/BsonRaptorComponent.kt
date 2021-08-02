package io.fluidsonic.raptor

import io.fluidsonic.raptor.RaptorBsonDefinition.*
import java.util.*
import org.bson.codecs.*
import org.bson.codecs.configuration.*


public class BsonRaptorComponent internal constructor() : RaptorComponent.Default<BsonRaptorComponent>() {

	internal val definitionsByPriority: MutableMap<Priority, MutableList<RaptorBsonDefinition>> = EnumMap(Priority::class.java)
	internal var includesDefaultDefinitions = false


	override fun RaptorComponentConfigurationEndScope.onConfigurationEnded() {
		// These codecs must come last to allow all other codecs to override default behavior.
		// Also, MongoDB would freak out with StackOverflowError if their own codecs don't come before these!
		val definitions =
			definitionsByPriority[Priority.high].orEmpty() +
				definitionsByPriority[Priority.normal].orEmpty() +
				definitionsByPriority[Priority.low].orEmpty() +
				if (includesDefaultDefinitions) RaptorBsonDefaults.definitions else emptyList()

		propertyRegistry.register(
			DefaultRaptorBsonProperties.Key,
			DefaultRaptorBsonProperties(context = lazyContext, definitions = definitions)
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
	definitions(codecs.map(RaptorBsonDefinition::of), priority = priority)
}


@RaptorDsl
public fun RaptorComponentSet<BsonRaptorComponent>.definitions(
	vararg definitions: RaptorBsonDefinition,
	priority: Priority = Priority.normal,
) {
	definitions(definitions.asIterable(), priority = priority)
}


@RaptorDsl
public fun RaptorComponentSet<BsonRaptorComponent>.definitions(
	definitions: Iterable<RaptorBsonDefinition>,
	priority: Priority = Priority.normal,
) {
	configure {
		definitionsByPriority.getOrPut(priority, ::mutableListOf).addAll(definitions)
	}
}


@RaptorDsl
public fun RaptorComponentSet<BsonRaptorComponent>.includeDefaultDefinitions() {
	configure {
		if (includesDefaultDefinitions)
			return@configure

		includesDefaultDefinitions = true
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
	definitions(providers.map(RaptorBsonDefinition::of), priority = priority)
}
