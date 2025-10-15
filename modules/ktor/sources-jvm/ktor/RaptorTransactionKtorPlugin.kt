package io.fluidsonic.raptor.ktor

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.transactions.*
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.util.*


private val attributeKey = AttributeKey<RaptorTransaction>("Raptor: server transaction")
private val ktorCallPropertyKey = RaptorPropertyKey<ApplicationCall>("Ktor call")


internal val RaptorTransactionKtorPlugin = createApplicationPlugin(
	name = "Raptor: transaction",
	createConfiguration = ::RaptorTransactionKtorPluginConfig,
) {
	val serverContext = checkNotNull(pluginConfig.serverContext) { "serverContext() not set." }
	val transactionFactory = checkNotNull(pluginConfig.transactionFactory) { "transactionFactory() not set." }

	on(CallSetup) { call ->
		call.attributes.put(attributeKey, transactionFactory.createTransaction(serverContext.transaction().context) {
			propertyRegistry.register(ktorCallPropertyKey, call)
		})
	}

	on(CallFailed) { call, cause ->
		call.attributes.remove(attributeKey)

		throw cause
	}

	on(ResponseSent) { call ->
		call.attributes.remove(attributeKey)
	}
}


public class RaptorTransactionKtorPluginConfig {

	internal var serverContext: RaptorContext? = null
		private set

	internal var transactionFactory: RaptorTransactionFactory? = null
		private set


	public fun serverContext(value: RaptorContext) {
		serverContext = value
	}


	public fun transactionFactory(value: RaptorTransactionFactory) {
		transactionFactory = value
	}
}


internal var ApplicationCall.raptorTransaction: RaptorTransaction
	get() = attributes.getOrNull(attributeKey) ?: throw RaptorPluginNotInstalledException(RaptorKtorPlugin)
	set(value) {
		attributes.put(attributeKey, value)
	}


internal val RaptorTransactionScope.ktorCall: ApplicationCall?
	get() = context[ktorCallPropertyKey]
