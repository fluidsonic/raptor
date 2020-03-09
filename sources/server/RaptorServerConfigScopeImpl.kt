package io.fluidsonic.raptor

import io.ktor.application.*
import org.kodein.di.*


// FIXME build GraphQL server on top of this
internal class RaptorServerConfigScopeImpl(
	private val globalFeatures: List<RaptorFeature<*>>,
	private val parentKodeinModule: Kodein.Module
) : RaptorServerConfigScope {

	private val featureFactories = mutableListOf<() -> RaptorServerFeature<*>>()
	private val kodeinConfigs = mutableListOf<Kodein.Builder.() -> Unit>()
	private val ktorConfigs = mutableListOf<Application.() -> Unit>()
	private val routeConfigs = mutableMapOf<String, RaptorRouteConfigScope.() -> Unit>()


	fun build(): RaptorServerConfig {
		for (globalFeature in globalFeatures) // FIXME what if overlap with server features? de-dup?
			with(globalFeature) {
				configure()
			}

		val features = featureFactories.invokeAll()

		val kodeinModule = Kodein.Module(name = "raptor/server") {
			import(parentKodeinModule)

			for (config in kodeinConfigs)
				config()
		}

		val ktorConfig = ktorConfigs.flatten()

		val routes = routeConfigs.map { (path, config) ->
			RaptorRouteConfigScopeImpl(
				globalFeatures = globalFeatures,
				parentFeatures = emptyList(),
				parentKodeinModule = kodeinModule,
				path = path,
				serverFeatures = features
			).apply(config).build()
		}

		return RaptorServerConfig(
			kodeinModule = kodeinModule,
			ktorConfig = ktorConfig,
			rootRouteConfig = routes
				.ifEmpty { null }
				?.let {
					RaptorRouteConfig(
						children = it,
						kodeinModule = kodeinModule,
						ktorConfig = {},
						path = ""
					)
				}
		)
	}


	override fun <ConfigDsl : Any> install(feature: RaptorServerFeature<ConfigDsl>, config: ConfigDsl.() -> Unit) {
		// FIXME check duplicates
		this.featureFactories += {
			feature.apply {
				configure(config) // FIXME scope kodein definitions into a module
			}
		}
	}


	override fun kodein(config: Kodein.Builder.() -> Unit) {
		kodeinConfigs += config
	}


	override fun ktor(config: Application.() -> Unit) {
		ktorConfigs += config
	}


	// FIXME generalize so that code between server routes and child routes can be reused
	override fun route(path: String, config: RaptorRouteConfigScope.() -> Unit) {
		val cleanedPath = path.trim('/')
		routeConfigs[cleanedPath] = config // FIXME check duplicates
	}
}
