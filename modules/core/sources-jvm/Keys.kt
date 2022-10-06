package io.fluidsonic.raptor


internal object Keys {

	val pluginConfigurationSet = RaptorPropertyKey<RaptorPluginConfigurationsSet>("plugin configuration set")
	val registrationComponentExtension = RaptorComponentExtensionKey<RaptorComponentRegistration<*>>("registration")
	val registryComponentExtension = RaptorComponentExtensionKey<RaptorComponentRegistry>("registry")
	val tagRegistryComponentExtension = RaptorComponentExtensionKey<RaptorComponentTagRegistry<*>>("tag registry")
}
