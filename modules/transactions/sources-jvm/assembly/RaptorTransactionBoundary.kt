package io.fluidsonic.raptor.transactions

import io.fluidsonic.raptor.*


// TODO Make Property boundary. How to make DI boundary?
public interface RaptorTransactionBoundary<Component : RaptorTransactionBoundary<Component>> : RaptorComponent<Component>


// TODO Support 'includeNested'.
// TODO Add proper (lazy) API to check for feature being installed.
@RaptorDsl
public val RaptorAssemblyQuery<RaptorTransactionBoundary<*>>.transactions: RaptorAssemblyQuery<RaptorTransactionsComponent>
	get() = map { component ->
		val registry = component.componentRegistry

		registry.root.oneOrNull(Keys.transactionsComponent) ?: throw RaptorFeatureNotInstalledException(RaptorTransactionFeature)
		registry.oneOrRegister(Keys.transactionsComponent, ::RaptorTransactionsComponent)
	}


// TODO Add proper (lazy) API to check for feature being installed.
@RaptorDsl
public fun RaptorComponentConfigurationEndScope<out RaptorTransactionBoundary<*>>.transactionFactory(): RaptorTransactionFactory {
	componentRegistry.root.oneOrNull(Keys.transactionsComponent) ?: throw RaptorFeatureNotInstalledException(RaptorTransactionFeature)

	return componentRegistry.oneOrNull(Keys.transactionsComponent)?.toFactory() ?: RaptorTransactionFactory.empty
}
