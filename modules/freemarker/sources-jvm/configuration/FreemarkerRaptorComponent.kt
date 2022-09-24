package io.fluidsonic.raptor

import freemarker.cache.*
import freemarker.template.*
import kotlin.reflect.*


public class FreemarkerRaptorComponent : RaptorComponent.Base<FreemarkerRaptorComponent>() {

	private val objectWrappers: MutableMap<KClass<*>, RaptorFreemarkerObjectWrapper<*>> = hashMapOf()
	private val templateLoaders: MutableList<TemplateLoader> = mutableListOf()


	@RaptorDsl
	public fun <Value : Any> objectWrapper(
		valueClass: KClass<out Value>,
		wrapper: RaptorFreemarkerObjectWrapper<Value>,
	) {
		objectWrappers[valueClass] = wrapper // FIXME catch duplicates
	}


	@RaptorDsl
	public fun resourceLoader(loader: KClass<*>, packagePath: String = "") {
		templateLoaders += ClassTemplateLoader(loader.java, packagePath)
	}


	@RaptorDsl
	public fun resourceLoader(loader: ClassLoader, packagePath: String = "") {
		templateLoaders += ClassTemplateLoader(loader, packagePath)
	}


	override fun RaptorComponentConfigurationEndScope<FreemarkerRaptorComponent>.onConfigurationEnded() {
		// FIXME make configurable
		// FIXME per-feature configuration?
		propertyRegistry.register(Configuration(Configuration.VERSION_2_3_30).apply {
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
}


@RaptorDsl
public inline fun <reified Value : Any> RaptorAssemblyQuery<FreemarkerRaptorComponent>.objectWrapper(
	wrapper: RaptorFreemarkerObjectWrapper<Value>,
) {
	objectWrapper(valueClass = Value::class, wrapper = wrapper)
}


@RaptorDsl
public fun <Value : Any> RaptorAssemblyQuery<FreemarkerRaptorComponent>.objectWrapper(
	valueClass: KClass<out Value>,
	wrapper: RaptorFreemarkerObjectWrapper<Value>,
) {
	this {
		objectWrapper(valueClass, wrapper)
	}
}


@RaptorDsl
public inline fun <reified ResourceClass : Any> RaptorAssemblyQuery<FreemarkerRaptorComponent>.resourceLoader(packagePath: String = "") {
	resourceLoader(loader = ResourceClass::class, packagePath = packagePath)
}


@RaptorDsl
public fun RaptorAssemblyQuery<FreemarkerRaptorComponent>.resourceLoader(loader: KClass<*>, packagePath: String = "") {
	this {
		resourceLoader(loader, packagePath)
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery<FreemarkerRaptorComponent>.resourceLoader(loader: ClassLoader, packagePath: String = "") {
	this {
		resourceLoader(loader, packagePath)
	}
}
