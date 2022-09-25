package io.fluidsonic.raptor

import kotlin.reflect.*


private val componentKey = RaptorComponentKey<RaptorSettingsComponent>("settings")


// TODO inject with DI? or should this only be used at assembly-time?
public interface RaptorSettings {

	public fun valueProvider(path: String): ValueProvider<*>?


	public companion object {

		public val empty: RaptorSettings = EmptyRaptorSettings


		public fun lookup(vararg settings: RaptorSettings): RaptorSettings =
			lookup(settings.toList())


		public fun lookup(settings: Iterable<RaptorSettings>): RaptorSettings =
			LookupRaptorSettings(settings.toList())
	}


	@RaptorDsl
	public class Builder internal constructor() {

		private val valueProviders: MutableMap<String, ValueProvider<*>> = hashMapOf()


		internal fun build(): RaptorSettings {
			if (valueProviders.isEmpty())
				return empty

			return MapRaptorSettings(valueProviders = valueProviders.toMap(hashMapOf()))
		}


		@RaptorDsl
		public fun set(key: String, value: Any?) {
			valueProviders[key] = value as? ValueProvider<*> ?: ValueProvider.constant(value)
		}


		@RaptorDsl
		public infix fun String.by(value: Any?) {
			set(this, value)
		}


		@RaptorDsl
		public operator fun String.invoke(values: Builder.() -> Unit) {
			set(this, Builder().apply(values).build())
		}
	}


	public interface ValueProvider<out Value : Any> {

		public val description: String
		public val value: Value?


		public companion object
	}
}


@Suppress("UNCHECKED_CAST")
private fun <Value : Any> convert(valueProvider: RaptorSettings.ValueProvider<*>, path: String, value: Any, type: KClass<Value>): Value {
	if (type == Int::class && value is String)
		value.toIntOrNull()?.let { return it as Value }

	return type.safeCast(value)
		?: error("Settings value '$path' (${valueProvider.description}) has ${value::class} but $type was expected.")
}


public fun RaptorSettings.int(path: String): Int =
	value(path)


public fun RaptorSettings.intOrNull(path: String): Int? =
	valueOrNull(path)


public fun RaptorSettings.settings(path: String): RaptorSettings =
	value(path)


public fun RaptorSettings.settingsOrNull(path: String): RaptorSettings? =
	valueOrNull(path)


public fun RaptorSettings.string(path: String): String =
	value(path)


public fun RaptorSettings.stringOrNull(path: String): String? =
	valueOrNull(path)


public inline fun <reified Value : Any> RaptorSettings.value(path: String): Value =
	value(path = path, type = Value::class)


public fun <Value : Any> RaptorSettings.value(path: String, type: KClass<out Value>): Value {
	val valueProvider = valueProvider(path)
		?: error("Settings value '$path' is not defined.")

	val anyValue = valueProvider.value
		?: error("Settings value '$path' (${valueProvider.description}) is not set.")

	return convert(valueProvider, path, anyValue, type)
}


public inline fun <reified Value : Any> RaptorSettings.valueOrNull(path: String): Value? =
	valueOrNull(path = path, type = Value::class)


public fun <Value : Any> RaptorSettings.valueOrNull(path: String, type: KClass<out Value>): Value? {
	val valueProvider = valueProvider(path) ?: return null
	val anyValue = valueProvider.value ?: return null

	return convert(valueProvider, path, anyValue, type)
}


public fun <Value : Any, TransformedValue : Any> RaptorSettings.ValueProvider<Value>.map(
	transform: (value: Value) -> TransformedValue?,
): RaptorSettings.ValueProvider<TransformedValue> =
	object : RaptorSettings.ValueProvider<TransformedValue> {

		override val description: String
			get() = this@map.description


		override val value: TransformedValue?
			get() = this@map.value?.let(transform)
	}


@RaptorDsl
public fun RaptorAssemblyInstallationScope.install(settings: RaptorSettings) {
	componentRegistry.oneOrNull(componentKey)?.let {
		error("Cannot set settings multiple times. Use RaptorSettings.lookup(…) to combine multiple settings.")
	}

	componentRegistry.register(componentKey, RaptorSettingsComponent(settings = settings))
}


@RaptorDsl
@Suppress("unused")
public fun RaptorGlobalDsl.settings(configure: RaptorSettings.Builder.() -> Unit): RaptorSettings =
	RaptorSettings.Builder().apply(configure).build()


@RaptorDsl
public val RaptorAssemblyScope.settings: RaptorSettings
	get() = componentRegistry.root.oneOrNull(componentKey)?.settings
		?: error("No settings have been registered. Use install(settings) inside raptor { … } before any other configuration.")
