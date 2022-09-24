package io.fluidsonic.raptor


internal object Keys {

	val registrationComponentExtension = RaptorComponentExtensionKey<RaptorComponentRegistration<*>>("registration")
	val registryComponentExtension = RaptorComponentExtensionKey<RaptorComponentRegistry>("registry")
	val rootComponentKey = RaptorComponentKey<RaptorRootComponent>("root")
	val tagRegistryComponentExtension = RaptorComponentExtensionKey<RaptorComponentTagRegistry<*>>("tag registry")
}
