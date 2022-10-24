package io.fluidsonic.raptor

import io.fluidsonic.raptor.graphql.internal.*
import kotlin.reflect.*


@RaptorDsl
public class RaptorAliasGraphDefinitionBuilder<Type : Any, ReferencedType : Any> internal constructor(
	private var isId: Boolean,
	private val kotlinType: KotlinType,
	private val referencedKotlinType: KotlinType,
	private val stackTrace: List<StackTraceElement>,
) {

	private var convertAliasToReferenced: (RaptorGraphOutputScope.(output: Any) -> Any)? = null
	private var convertReferencedToAlias: (RaptorGraphInputScope.(input: Any) -> Any)? = null


	init {
		check(!isId || referencedKotlinType.classifier == String::class) { "A GraphQL 'ID' alias must reference Kotlin type 'String'." }
		check(!kotlinType.isNullable) { "Type must not be nullable: $kotlinType" }
		check(!referencedKotlinType.isNullable) { "Referenced type must not be nullable: $referencedKotlinType" }
	}


	internal fun build() =
		AliasGraphTypeDefinition(
			additionalDefinitions = emptyList(),
			isId = isId,
			convertReferencedToAlias = checkNotNull(convertReferencedToAlias) { "Parsing must be defined: parse { … }" },
			convertAliasToReferenced = checkNotNull(convertAliasToReferenced) { "Serializing must be defined: serialize { … }" },
			isInput = true,
			isOutput = true,
			kotlinType = kotlinType,
			referencedKotlinType = referencedKotlinType,
			stackTrace = stackTrace
		)


	// TODO rn
	@RaptorDsl
	public fun parse(convert: RaptorGraphInputScope.(input: ReferencedType) -> Type) {
		check(this.convertReferencedToAlias === null) { "Cannot define multiple parsers." }

		@Suppress("UNCHECKED_CAST")
		this.convertReferencedToAlias = convert as RaptorGraphInputScope.(input: Any) -> Any
	}


	// TODO rn
	@RaptorDsl
	public fun serialize(convert: RaptorGraphOutputScope.(output: Type) -> ReferencedType) {
		check(this.convertAliasToReferenced === null) { "Cannot define multiple serializers." }

		@Suppress("UNCHECKED_CAST")
		this.convertAliasToReferenced = convert as RaptorGraphOutputScope.(output: Any) -> Any
	}


	// TODO rn
	@RaptorDsl
	public fun serialize(convert: KFunction1<Type, ReferencedType>) {
		serialize { convert(it) }
	}
}
