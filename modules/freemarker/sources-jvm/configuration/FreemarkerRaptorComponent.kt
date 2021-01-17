package io.fluidsonic.raptor

import freemarker.cache.*
import freemarker.template.*
import kotlin.reflect.*


public class FreemarkerRaptorComponent : RaptorComponent.Default<FreemarkerRaptorComponent>() {

	internal val objectWrappers: MutableMap<KClass<*>, RaptorFreemarkerObjectWrapper<*>> = hashMapOf()
	internal val templateLoaders: MutableList<TemplateLoader> = mutableListOf()


	override fun RaptorComponentConfigurationEndScope.onConfigurationEnded() {
		// FIXME make configurable
		// FIXME per-feature configuration?
		propertyRegistry.register(FreemarkerRaptorPropertyKey, Configuration(Configuration.VERSION_2_3_30).apply {
			defaultEncoding = Charsets.UTF_8.name()
			logTemplateExceptions = false
			templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
			templateLoader = MultiTemplateLoader(templateLoaders.toTypedArray())
			templateUpdateDelayMilliseconds = Long.MAX_VALUE
			urlEscapingCharset = Charsets.UTF_8.name()

			if (objectWrappers.isNotEmpty())
				objectWrapper = DelegatingFreemarkerObjectWrapper(objectWrappers)
		})
	}


	internal object Key : RaptorComponentKey<FreemarkerRaptorComponent> {

		override fun toString() = "freemarker"
	}
}


@RaptorDsl
public val RaptorTopLevelConfigurationScope.freemarker: RaptorComponentSet<FreemarkerRaptorComponent>
	get() = componentRegistry.configure(FreemarkerRaptorComponent.Key)


@RaptorDsl
public inline fun <reified Value : Any> RaptorComponentSet<FreemarkerRaptorComponent>.objectWrapper(
	wrapper: RaptorFreemarkerObjectWrapper<Value>,
) {
	objectWrapper(valueClass = Value::class, wrapper = wrapper)
}


@RaptorDsl
public fun <Value : Any> RaptorComponentSet<FreemarkerRaptorComponent>.objectWrapper(
	valueClass: KClass<out Value>,
	wrapper: RaptorFreemarkerObjectWrapper<Value>,
) {
	configure {
		objectWrappers[valueClass] = wrapper // FIXME catch duplicates
	}
}


@RaptorDsl
public inline fun <reified ResourceClass : Any> RaptorComponentSet<FreemarkerRaptorComponent>.resourceLoader(packagePath: String = "") {
	resourceLoader(loader = ResourceClass::class, packagePath = packagePath)
}


@RaptorDsl
public fun RaptorComponentSet<FreemarkerRaptorComponent>.resourceLoader(loader: KClass<*>, packagePath: String = "") {
	configure {
		templateLoaders += ClassTemplateLoader(loader.java, packagePath)
	}
}


@RaptorDsl
public fun RaptorComponentSet<FreemarkerRaptorComponent>.resourceLoader(loader: ClassLoader, packagePath: String = "") {
	configure {
		templateLoaders += ClassTemplateLoader(loader, packagePath)
	}
}
