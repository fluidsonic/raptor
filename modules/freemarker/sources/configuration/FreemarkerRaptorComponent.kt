package io.fluidsonic.raptor

import freemarker.cache.*
import freemarker.template.*
import kotlin.reflect.*


class FreemarkerRaptorComponent : RaptorComponent.Default<FreemarkerRaptorComponent>() {

	internal val templateLoaders = mutableListOf<TemplateLoader>()


	override fun RaptorComponentConfigurationEndScope.onConfigurationEnded() {
		// FIXME make configurable
		// FIXME per-feature configuration?
		propertyRegistry.register(FreemarkerRaptorPropertyKey, Configuration(Configuration.VERSION_2_3_30).apply {
			defaultEncoding = Charsets.UTF_8.name()
			logTemplateExceptions = false
			templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
			templateLoader = MultiTemplateLoader(templateLoaders.toTypedArray())
			templateUpdateDelayMilliseconds = Long.MAX_VALUE
		})
	}


	internal object Key : RaptorComponentKey<FreemarkerRaptorComponent> {

		override fun toString() = "freemarker"
	}
}


@RaptorDsl
val RaptorTopLevelConfigurationScope.freemarker: RaptorComponentSet<FreemarkerRaptorComponent>
	get() = componentRegistry.configure(FreemarkerRaptorComponent.Key)


@RaptorDsl
inline fun <reified ResourceClass : Any> RaptorComponentSet<FreemarkerRaptorComponent>.resourceLoader(packagePath: String = "") =
	resourceLoader(loader = ResourceClass::class, packagePath = packagePath)


@RaptorDsl
fun RaptorComponentSet<FreemarkerRaptorComponent>.resourceLoader(loader: KClass<*>, packagePath: String = "") {
	configure {
		templateLoaders += ClassTemplateLoader(loader.java, packagePath)
	}
}


@RaptorDsl
fun RaptorComponentSet<FreemarkerRaptorComponent>.resourceLoader(loader: ClassLoader, packagePath: String = "") {
	configure {
		templateLoaders += ClassTemplateLoader(loader, packagePath)
	}
}
