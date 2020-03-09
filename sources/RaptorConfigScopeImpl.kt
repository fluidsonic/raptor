package io.fluidsonic.raptor

import org.kodein.di.*


internal class RaptorConfigScopeImpl : RaptorConfigScope {

	private val featureFactories = mutableListOf<() -> RaptorFeature<*>>()
	private val kodeinConfigs = mutableListOf<Kodein.Builder.() -> Unit>()
	private var serverConfig: (RaptorServerConfigScope.() -> Unit)? = null
	private val startCallbacks = mutableListOf<suspend RaptorScope.() -> Unit>()
	private val stopCallbacks = mutableListOf<suspend RaptorScope.() -> Unit>()


	fun build(): RaptorConfig {
		val kodeinModule = Kodein.Module(name = "raptor") {
			for (config in kodeinConfigs)
				config()
		}

		val features = featureFactories.invokeAll()

		val serverConfig = serverConfig?.let { config ->
			RaptorServerConfigScopeImpl(
				globalFeatures = features,
				parentKodeinModule = kodeinModule
			).apply(config).build()
		}

		return RaptorConfig(
			kodeinModule = kodeinModule,
			serverConfig = serverConfig,
			startCallbacks = startCallbacks,
			stopCallbacks = stopCallbacks
		)
	}


	override fun <ConfigDsl : Any> install(feature: RaptorFeature<ConfigDsl>, config: ConfigDsl.() -> Unit) {
		// FIXME check duplicates
		featureFactories += {
			feature.apply {
				configure(config) // FIXME scope kodein definitions into a module
			}
		}
	}


	override fun kodein(config: Kodein.Builder.() -> Unit) {
		kodeinConfigs += config
	}


	override fun onStart(callback: suspend RaptorScope.() -> Unit) {
		startCallbacks += callback
	}


	override fun onStop(callback: suspend RaptorScope.() -> Unit) {
		stopCallbacks += callback
	}


	override fun server(config: RaptorServerConfigScope.() -> Unit) {
		check(serverConfig == null) { "Cannot define multiple servers: server { … }" }

		serverConfig = config
	}
}
