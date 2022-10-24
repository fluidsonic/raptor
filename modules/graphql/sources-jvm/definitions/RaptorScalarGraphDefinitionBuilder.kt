package io.fluidsonic.raptor

import io.fluidsonic.raptor.graphql.internal.*
import kotlin.reflect.*


@RaptorDsl
public class RaptorScalarGraphDefinitionBuilder<Type : Any> internal constructor(
	kotlinType: KotlinType,
	name: String,
	private val stackTrace: List<StackTraceElement>,
) : RaptorNamedGraphTypeDefinitionBuilder<Type>(
	kotlinType = kotlinType,
	name = name
) {

	private var parse: (RaptorGraphInputScope.(input: Any) -> Any)? = null
	private var serialize: (RaptorGraphOutputScope.(output: Any) -> Any)? = null


	override fun build(description: String?) = ScalarGraphDefinition(
		additionalDefinitions = emptyList(),
		description = description,
		isInput = true,
		isOutput = true,
		kotlinType = kotlinType,
		name = name,
		parse = parse ?: error("A parser must be defined: parse(Boolean/Int/Float/Object/String) { … }"),
		serialize = serialize ?: error("A serializer must be defined: serialize { … }"),
		stackTrace = stackTrace
	)


	@RaptorDsl
	public fun parse(parse: KFunction1<Any, Type>) {
		this.parse { input ->
			parse(input)
		}
	}


	@RaptorDsl
	public fun parse(parse: RaptorGraphInputScope.(input: Any) -> Type) {
		check(this.parse === null) { "Cannot define multiple parsers." }

		this.parse = parse
	}


	@RaptorDsl
	public fun parseBoolean(parse: KFunction1<Boolean, Type>) {
		parseBoolean { input ->
			parse(input)
		}
	}


	@RaptorDsl
	public fun parseBoolean(parse: RaptorGraphInputScope.(input: Boolean) -> Type) {
		this.parse { input ->
			parse(input as? Boolean ?: invalid())
		}
	}


	@RaptorDsl
	public fun parseFloat(parse: KFunction1<Double, Type>) {
		parseFloat { input ->
			parse(input)
		}
	}


	@RaptorDsl
	public fun parseFloat(parse: RaptorGraphInputScope.(input: Double) -> Type) {
		this.parse { input ->
			parse(input as? Double ?: invalid())
		}
	}


	@RaptorDsl
	public fun parseInt(parse: KFunction1<Int, Type>) {
		parseInt { input ->
			parse(input)
		}
	}


	@RaptorDsl
	public fun parseInt(parse: RaptorGraphInputScope.(input: Int) -> Type) {
		this.parse { input ->
			parse(input as? Int ?: invalid())
		}
	}


	@RaptorDsl
	public fun parseObject(parse: KFunction1<Map<String, *>, Type>) {
		parseObject { input ->
			parse(input)
		}
	}


	@RaptorDsl
	@Suppress("UNCHECKED_CAST")
	public fun parseObject(parse: RaptorGraphInputScope.(input: Map<String, *>) -> Type) {
		this.parse { input ->
			parse(input as? Map<String, *> ?: invalid())
		}
	}


	@RaptorDsl
	public fun parseString(parse: KFunction1<String, Type>) {
		parseString { input ->
			parse(input)
		}
	}


	@RaptorDsl
	public fun parseString(parse: RaptorGraphInputScope.(input: String) -> Type) {
		this.parse { input ->
			parse(input as? String ?: invalid())
		}
	}


	@RaptorDsl
	public fun serialize(serialize: KFunction1<Type, Any>) {
		serialize { serialize(it) }
	}


	@RaptorDsl
	public fun serialize(serialize: KProperty1<Type, Any>) {
		serialize(serialize::invoke)
	}


	@RaptorDsl
	@Suppress("UNCHECKED_CAST")
	public fun serialize(serialize: RaptorGraphOutputScope.(output: Type) -> Any) {
		check(this.serialize == null) { "Cannot define multiple serializers." }

		this.serialize = serialize as RaptorGraphOutputScope.(output: Any) -> Any
	}
}
