package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*
import kotlin.reflect.*


public class RaptorGraphComponent internal constructor() :
	RaptorComponent.Base<RaptorGraphComponent>(RaptorGraphPlugin),
	RaptorTaggableComponent<RaptorGraphComponent> {

	@RaptorDsl
	public val definitions: Definitions = Definitions()


	internal fun complete(): RaptorGraph =
		GraphSystemDefinitionBuilder.build(definitions.list)
			.let(GraphTypeSystemBuilder::build)
			.let { GraphSystemBuilder.build(tags = tags(), typeSystem = it) }


	public inner class Definitions : RaptorAssemblyQuery<Definitions> {

		private var includesDefault = false

		internal val list: MutableList<RaptorGraphDefinition> = mutableListOf()


		@RaptorDsl
		public fun add(definitions: Iterable<RaptorGraphDefinition>) {
			list += definitions
		}


		@RaptorDsl
		override fun each(configure: Definitions.() -> Unit) {
			configure()
		}


		@RaptorDsl
		public fun includeDefault() {
			if (includesDefault)
				return

			includesDefault = true

			add(RaptorGraphDefaults.definitions)
		}
	}
}


@RaptorDsl
public val RaptorAssemblyQuery<RaptorGraphComponent>.definitions: RaptorAssemblyQuery<RaptorGraphComponent.Definitions>
	get() = map { it.definitions }


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorGraphComponent.Definitions>.add(vararg definitions: RaptorGraphDefinition) {
	add(definitions.asIterable())
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorGraphComponent.Definitions>.add(definitions: Iterable<RaptorGraphDefinition>) {
	this {
		add(definitions)
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorGraphComponent.Definitions>.includeDefault() {
	this {
		includeDefault()
	}
}


@RaptorDsl
public inline fun <reified Type : Enum<Type>> RaptorAssemblyQuery<RaptorGraphComponent.Definitions>.newEnum(
	name: String = RaptorGraphDefinition.defaultName,
	@BuilderInference noinline configure: RaptorEnumGraphDefinitionBuilder<Type>.() -> Unit = {},
) {
	add(graphEnumDefinition(
		name = name,
		type = typeOf<Type>(),
		values = enumValues<Type>().toList(),
		configure = configure,
	))
}


@RaptorDsl
public inline fun <reified Type : Any> RaptorAssemblyQuery<RaptorGraphComponent.Definitions>.newIdAlias(
	@BuilderInference noinline configure: RaptorAliasGraphDefinitionBuilder<Type, String>.() -> Unit,
) {
	add(graphIdAliasDefinition(
		type = typeOf<Type>(),
		configure = configure,
	))
}


@RaptorDsl
public inline fun <reified Type : Any> RaptorAssemblyQuery<RaptorGraphComponent.Definitions>.newInputObject(
	name: String = RaptorGraphDefinition.defaultName,
	@BuilderInference noinline configure: RaptorInputObjectGraphDefinitionBuilder<Type>.() -> Unit,
) {
	add(graphInputObjectDefinition(
		name = name,
		type = typeOf<Type>(),
		configure = configure,
	))
}


@RaptorDsl
public fun <Type : Any> RaptorAssemblyQuery<RaptorGraphComponent.Definitions>.newInterface(
	name: String = RaptorGraphDefinition.defaultName,
	type: KType,
	configure: RaptorInterfaceGraphDefinitionBuilder<Type>.() -> Unit,
) {
	add(graphInterfaceDefinition(name = name, type = type, configure = configure))
}


@RaptorDsl
public inline fun <reified Type : Any> RaptorAssemblyQuery<RaptorGraphComponent.Definitions>.newInterface(
	name: String = RaptorGraphDefinition.defaultName,
	noinline configure: RaptorInterfaceGraphDefinitionBuilder<Type>.() -> Unit,
) {
	newInterface(
		name = name,
		type = typeOf<Type>(),
		configure = configure
	)
}


@RaptorDsl
public fun <Type : Any> RaptorAssemblyQuery<RaptorGraphComponent.Definitions>.newObject(
	name: String = RaptorGraphDefinition.defaultName,
	type: KType,
	configure: RaptorObjectGraphDefinitionBuilder<Type>.() -> Unit = {},
) {
	add(graphObjectDefinition(name = name, type = type, configure = configure))
}


@RaptorDsl
public inline fun <reified Type : Any> RaptorAssemblyQuery<RaptorGraphComponent.Definitions>.newObject(
	name: String = RaptorGraphDefinition.defaultName,
	@BuilderInference noinline configure: RaptorObjectGraphDefinitionBuilder<Type>.() -> Unit = {},
) {
	newObject(name = name, type = typeOf<Type>(), configure = configure)
}


@RaptorDsl
public inline fun <reified Value : Any> RaptorAssemblyQuery<RaptorGraphComponent.Definitions>.newScalar(
	name: String = RaptorGraphDefinition.defaultName,
	noinline configure: RaptorScalarGraphDefinitionBuilder<Value>.() -> Unit,
) {
	add(graphScalarDefinition(
		name = name,
		type = typeOf<Value>(),
		configure = configure,
	))
}


@RaptorDsl
public inline fun <reified Type : Any> RaptorAssemblyQuery<RaptorGraphComponent.Definitions>.newUnion(
	name: String = RaptorGraphDefinition.defaultName,
	@BuilderInference noinline configure: RaptorUnionGraphDefinitionBuilder<Type>.() -> Unit = {},
) {
	add(graphUnionDefinition(
		name = name,
		type = typeOf<Type>(),
		configure = configure,
	))
}
