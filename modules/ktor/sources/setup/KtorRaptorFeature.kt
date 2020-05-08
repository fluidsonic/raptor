package io.fluidsonic.raptor

import com.typesafe.config.*
import io.ktor.config.*
import org.kodein.di.erased.*


object KtorRaptorFeature : RaptorFeature {

	override fun RaptorFeatureSetup.setup() {
		componentRegistry.register(KtorRaptorComponent(globalComponent = this))

		kodein {
			// FIXME Rework this. We need a global config feature instead.
			bind<ApplicationConfig>() with singleton {
				HoconApplicationConfig(ConfigFactory.defaultApplication().resolve()!!)
			}
		}
	}


	override fun RaptorFeatureSetupCompletion.completeSetup() {
		val config = componentRegistry.getSingle<KtorRaptorComponent>()?.component?.complete(globalCompletion = this) ?: return

		kodein {
			bind<KtorImpl>() with singleton {
				KtorImpl(
					config = config,
					parentContext = instance()
				)
			}
		}

		onStart {
			instance<KtorImpl>().start()
		}

		onStop {
			instance<KtorImpl>().stop()
		}
	}
}


@Raptor.Dsl3
val RaptorFeatureComponent.ktor: RaptorComponentSet<KtorRaptorComponent>
	get() {
		install(KtorRaptorFeature)

		return componentRegistry.configureSingle()
	}
