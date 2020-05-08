package io.fluidsonic.raptor

import freemarker.cache.*
import freemarker.template.*
import org.kodein.di.erased.*


object FreemarkerFeature : RaptorFeature {

	override fun RaptorFeatureSetup.setup() {
		componentRegistry.register(FreemarkerComponent())
	}


	override fun RaptorFeatureSetupCompletion.completeSetup() {
		val component = checkNotNull(componentRegistry.getSingle<FreemarkerComponent>()?.component) // FIXME .requireSingle
		val templateLoaders = component.templateLoaders.toTypedArray()

		kodein {
			bind<Configuration>() with singleton {
				// FIXME make configurable
				// FIXME per-feature configuration?
				Configuration(Configuration.VERSION_2_3_30).apply {
					defaultEncoding = Charsets.UTF_8.name()
					logTemplateExceptions = false
					templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
					templateLoader = MultiTemplateLoader(templateLoaders)
					templateUpdateDelayMilliseconds = Long.MAX_VALUE
				}
			}
		}
	}
}


// FIXME is it okay to automatically register the feature?
// FIXME better name
@Raptor.Dsl3
val RaptorFeatureComponent.freemarker: RaptorComponentSet<FreemarkerComponent>
	get() {
		install(FreemarkerFeature)

		return componentRegistry.configureSingle()
	}
