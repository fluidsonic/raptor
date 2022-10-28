package io.fluidsonic.raptor.graph

import io.fluidsonic.raptor.*


@RaptorDsl
public class RaptorEnumGraphDefinitionBuilder<Type : Enum<Type>> internal constructor(
	kotlinType: KotlinType,
	name: String,
	private val stackTrace: List<StackTraceElement>,
	private val values: Collection<Type>,
) : RaptorNamedGraphTypeDefinitionBuilder<Type>(
	kotlinType = kotlinType,
	name = name
) {

	private val explicitValues: HashMap<String, EnumValue> = hashMapOf()


	override fun build(description: String?) =
		EnumGraphDefinition(
			additionalDefinitions = emptyList(),
			description = description,
			isInput = true,
			isOutput = true,
			kotlinType = kotlinType,
			name = name,
			parse = { name ->
				this@RaptorEnumGraphDefinitionBuilder.values.firstOrNull { it.name == name }
					?: invalid(details = "valid values: ${
						this@RaptorEnumGraphDefinitionBuilder.values.sortedBy { it.name }.joinToString(separator = ", ") { it.name }
					}")
			}, // TODO leak. rework
			serialize = { (it as Type).name }, // TODO rework
			stackTrace = stackTrace,
			values = explicitValues
				.ifEmpty { null }
				?.values
				?.toList()
				?: values.map { EnumValue(description = null, name = it.name) }
		)


	@RaptorDsl
	public fun value(
		value: Type,
		configure: RaptorEnumValueGraphDefinitionBuilder.() -> Unit = {},
	) {
		check(!explicitValues.containsKey(value.name)) { "Cannot define value '${value.name}' multiple times." }

		explicitValues[value.name] = RaptorEnumValueGraphDefinitionBuilder(name = value.name).apply(configure).build()
	}
}
