package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*
import kotlin.reflect.*


public class RaptorGraphComponent internal constructor() : RaptorComponent2.Base<RaptorGraphComponent>(), RaptorTaggableComponent2 {

	internal var graph: RaptorGraph? = null
		private set


	@RaptorDsl
	public val definitions: Definitions = Definitions()


	override fun RaptorComponentConfigurationEndScope2.onConfigurationEnded() {
		graph = GraphSystemDefinitionBuilder.build(definitions.list)
			.let(GraphTypeSystemBuilder::build)
			.let { GraphSystemBuilder.build(tags = tags(this@RaptorGraphComponent), typeSystem = it) }
	}


	public inner class Definitions : RaptorAssemblyQuery2<Definitions> {

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


	internal object Key : RaptorComponentKey2<RaptorGraphComponent> {

		override fun toString() = "graph"
	}
}


@RaptorDsl
public val RaptorAssemblyQuery2<RaptorGraphComponent>.definitions: RaptorAssemblyQuery2<RaptorGraphComponent.Definitions>
	get() = map { it.definitions }


@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorGraphComponent.Definitions>.add(vararg definitions: RaptorGraphDefinition) {
	add(definitions.asIterable())
}


@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorGraphComponent.Definitions>.add(definitions: Iterable<RaptorGraphDefinition>) {
	this {
		add(definitions)
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorGraphComponent.Definitions>.includeDefault() {
	this {
		includeDefault()
	}
}


@RaptorDsl
public inline fun <reified Type : Enum<Type>> RaptorAssemblyQuery2<RaptorGraphComponent.Definitions>.newEnum(
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
public inline fun <reified Type : Any> RaptorAssemblyQuery2<RaptorGraphComponent.Definitions>.newIdAlias(
	@BuilderInference noinline configure: RaptorAliasGraphDefinitionBuilder<Type, String>.() -> Unit,
) {
	add(graphIdAliasDefinition(
		type = typeOf<Type>(),
		configure = configure,
	))
}


@RaptorDsl
public inline fun <reified Type : Any> RaptorAssemblyQuery2<RaptorGraphComponent.Definitions>.newInputObject(
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
public inline fun <reified Type : Any> RaptorAssemblyQuery2<RaptorGraphComponent.Definitions>.newObject(
	name: String = RaptorGraphDefinition.defaultName,
	@BuilderInference noinline configure: RaptorObjectGraphDefinitionBuilder<Type>.() -> Unit = {},
) {
	add(graphObjectDefinition(
		name = name,
		type = typeOf<Type>(),
		configure = configure,
	))
}


@RaptorDsl
public inline fun <reified Value : Any> RaptorAssemblyQuery2<RaptorGraphComponent.Definitions>.newScalar(
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
public inline fun <reified Type : Any> RaptorAssemblyQuery2<RaptorGraphComponent.Definitions>.newUnion(
	name: String = RaptorGraphDefinition.defaultName,
	@BuilderInference noinline configure: RaptorUnionGraphDefinitionBuilder<Type>.() -> Unit = {},
) {
	add(graphUnionDefinition(
		name = name,
		type = typeOf<Type>(),
		configure = configure,
	))
}


// We can either use two phases:
//  1. complete conf (create RaptorGraph)
//  2. end conf (reference RaptorGraph from other component)
// or we add some form of dependency system
// or we request an early configuration end on-demand (can lead to cycles which must be detected)
// Note that it's not yet possible to define graphs below the root component. But it might be at some point.
// FIXME might need requireFeature?
@RaptorDsl
public fun RaptorComponentConfigurationEndScope2.graph(tag: Any? = null): RaptorGraph? {
	fun RaptorComponentRegistry2.find(): RaptorGraph? {
		oneOrNull(RaptorGraphsComponent.Key)
			?.componentRegistry2
			?.many(RaptorGraphComponent.Key)
			?.filter { tag == null || tags(it).contains(tag) }
			?.also { check(it.size <= 1) { if (tag != null) "Found multiple graphs with tag: $tag" else "Found multiple graphs" } }
			?.firstOrNull()
			?.let { component ->
				component.endConfiguration()

				return checkNotNull(component.graph)
			}

		return parent?.find()
	}

	return componentRegistry2.find()
}
