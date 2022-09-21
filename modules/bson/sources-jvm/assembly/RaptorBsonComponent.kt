package io.fluidsonic.raptor.bson

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.RaptorBsonDefinition.*
import java.util.*
import org.bson.codecs.*
import org.bson.codecs.configuration.*


public class RaptorBsonComponent internal constructor() : RaptorComponent2.Base() {

	private val definitionsByPriority: MutableMap<Priority, MutableList<RaptorBsonDefinition>> = EnumMap(Priority::class.java)
	private var includesDefaultDefinitions = false


	@RaptorDsl
	public fun codecs(vararg codecs: Codec<*>, priority: Priority = Priority.normal) {
		codecs(codecs.asIterable(), priority = priority)
	}


	@RaptorDsl
	public fun codecs(codecs: Iterable<Codec<*>>, priority: Priority = Priority.normal) {
		definitions(codecs.map(RaptorBsonDefinition::of), priority = priority)
	}


	@RaptorDsl
	public inline fun <reified Value : Any> definition(
		@BuilderInference noinline configure: RaptorBsonDefinitionBuilder<Value>.() -> Unit,
	) {
		definitions(raptor.bson.definition(configure))
	}


	@RaptorDsl
	public fun definitions(vararg definitions: RaptorBsonDefinition, priority: Priority = Priority.normal) {
		definitions(definitions.asIterable(), priority = priority)
	}


	@RaptorDsl
	public fun definitions(definitions: Iterable<RaptorBsonDefinition>, priority: Priority = Priority.normal) {
		definitionsByPriority.getOrPut(priority, ::mutableListOf).addAll(definitions)
	}


	@RaptorDsl
	public fun includeDefaultDefinitions() {
		if (includesDefaultDefinitions)
			return

		includesDefaultDefinitions = true
	}


	@RaptorDsl
	public fun providers(vararg providers: CodecProvider, priority: Priority = Priority.normal) {
		providers(providers.asIterable(), priority = priority)
	}


	@RaptorDsl
	public fun providers(providers: Iterable<CodecProvider>, priority: Priority = Priority.normal) {
		definitions(providers.map(RaptorBsonDefinition::of), priority = priority)
	}


	override fun RaptorComponentConfigurationEndScope2.onConfigurationEnded() {
		// These codecs must come last to allow all other codecs to override default behavior.
		// Also, MongoDB would freak out with StackOverflowError if their own codecs don't come before these!
		val definitions =
			definitionsByPriority[Priority.high].orEmpty() +
				definitionsByPriority[Priority.normal].orEmpty() +
				definitionsByPriority[Priority.low].orEmpty() +
				if (includesDefaultDefinitions) RaptorBsonDefinition.defaults else emptyList()

		propertyRegistry.register(RaptorBsonKey, DefaultRaptorBson(context = lazyContext, definitions = definitions))
	}


	public companion object;


	internal object Key : RaptorComponentKey2<RaptorBsonComponent> {

		override fun toString() = "bson"
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorBsonComponent>.codecs(
	vararg codecs: Codec<*>,
	priority: Priority = Priority.normal,
) {
	codecs(codecs.asIterable(), priority = priority)
}


@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorBsonComponent>.codecs(
	codecs: Iterable<Codec<*>>,
	priority: Priority = Priority.normal,
) {
	definitions(codecs.map(RaptorBsonDefinition::of), priority = priority)
}


@RaptorDsl
public inline fun <reified Value : Any> RaptorAssemblyQuery2<RaptorBsonComponent>.definition(
	@BuilderInference noinline configure: RaptorBsonDefinitionBuilder<Value>.() -> Unit,
) {
	definitions(raptor.bson.definition(configure))
}


@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorBsonComponent>.definitions(
	vararg definitions: RaptorBsonDefinition,
	priority: Priority = Priority.normal,
) {
	definitions(definitions.asIterable(), priority = priority)
}


@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorBsonComponent>.definitions(
	definitions: Iterable<RaptorBsonDefinition>,
	priority: Priority = Priority.normal,
) {
	this {
		definitions(definitions, priority = priority)
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorBsonComponent>.includeDefaultDefinitions() {
	this {
		includeDefaultDefinitions()
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorBsonComponent>.providers(
	vararg providers: CodecProvider,
	priority: Priority = Priority.normal,
) {
	providers(providers.asIterable(), priority = priority)
}


@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorBsonComponent>.providers(
	providers: Iterable<CodecProvider>,
	priority: Priority = Priority.normal,
) {
	definitions(providers.map(RaptorBsonDefinition::of), priority = priority)
}
