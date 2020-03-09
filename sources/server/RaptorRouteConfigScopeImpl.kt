package io.fluidsonic.raptor

import io.ktor.routing.*
import org.kodein.di.*


internal class RaptorRouteConfigScopeImpl(
	private val globalFeatures: List<RaptorFeature<*>>,
	private val parentFeatures: List<RaptorRouteFeature<*>>,
	private val parentKodeinModule: Kodein.Module,
	private val path: String,
	private val serverFeatures: List<RaptorServerFeature<*>>
) : RaptorRouteConfigScope {

	private val featureConfigs = mutableListOf<() -> RaptorRouteFeature<*>>()
	private val kodeinConfigs = mutableListOf<Kodein.Builder.() -> Unit>()
	private val ktorConfigs = mutableListOf<Route.() -> Unit>()
	private val routeConfigs = mutableMapOf<String, RaptorRouteConfigScope.() -> Unit>()


	fun build(): RaptorRouteConfig {
		for (globalFeature in globalFeatures) // FIXME what if overlap with server features? de-dup?
			with(globalFeature) {
				configure()
			}

		for (serverFeature in serverFeatures) // FIXME what if overlap with server features? de-dup?
			with(serverFeature) {
				configure()
			}

		for (parentFeature in parentFeatures) // FIXME what if overlap with server features? de-dup?
			with(parentFeature) {
				configure()
			}

		val features = featureConfigs.map { it() }

		val kodeinModule = Kodein.Module(name = "raptor/server/route[path=$path]") {
			import(parentKodeinModule)

			for (config in kodeinConfigs)
				config()
		}

		val ktorConfig = ktorConfigs.flatten()

		val routes = routeConfigs.map { (childPath, config) ->
			RaptorRouteConfigScopeImpl(
				globalFeatures = globalFeatures,
				parentFeatures = features,
				parentKodeinModule = kodeinModule,
				serverFeatures = serverFeatures,
				path = "$path/$childPath"
			).apply(config).build()
		}

		return RaptorRouteConfig(
			children = routes,
			kodeinModule = kodeinModule,
			ktorConfig = ktorConfig,
			path = path
		)
	}


	override fun <ConfigDsl : Any> install(feature: RaptorRouteFeature<ConfigDsl>, config: ConfigDsl.() -> Unit) {
		// FIXME check duplicates
		this.featureConfigs += {
			feature.apply {
				configure(config) // FIXME scope kodein definitions into a module
			}
		}
	}


	override fun kodein(config: Kodein.Builder.() -> Unit) {
		kodeinConfigs += config
	}


	override fun ktor(config: Route.() -> Unit) {
		ktorConfigs += config
	}


	override fun route(path: String, config: RaptorRouteConfigScope.() -> Unit) {
		val cleanedPath = path.trim('/')
		routeConfigs[cleanedPath] = config // FIXME check duplicates
	}
}
