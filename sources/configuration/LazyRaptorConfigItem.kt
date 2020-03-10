package io.fluidsonic.raptor


interface LazyRaptorConfigItem<Config : Any, Item : RaptorSetupRegistration<Config>> {

	operator fun invoke(config: Config.() -> Unit)
}
