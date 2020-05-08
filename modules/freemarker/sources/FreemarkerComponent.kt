package io.fluidsonic.raptor

import freemarker.cache.*
import kotlin.reflect.*


class FreemarkerComponent : RaptorComponent {

	internal val templateLoaders = mutableListOf<TemplateLoader>()


	inline fun <reified ResourceClass : Any> resourceLoader(packagePath: String = "") =
		resourceLoader(loader = ResourceClass::class, packagePath = packagePath)


	fun resourceLoader(loader: KClass<*>, packagePath: String = "") {
		templateLoaders += ClassTemplateLoader(loader.java, packagePath)
	}


	fun resourceLoader(loader: ClassLoader, packagePath: String = "") {
		templateLoaders += ClassTemplateLoader(loader, packagePath)
	}
}
