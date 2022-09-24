package io.fluidsonic.raptor.transactions

import io.fluidsonic.raptor.*


public class RaptorTransactionsComponent internal constructor() : RaptorComponent.Base<RaptorTransactionsComponent>() {

	private val configurations: MutableList<RaptorTransactionConfigurationScope.() -> Unit> = mutableListOf()
	private val observers: MutableList<RaptorTransactionObserver> = mutableListOf()


	@RaptorDsl
	public fun onCreate(action: RaptorTransactionConfigurationScope.() -> Unit) {
		configurations += action
	}


	@RaptorDsl
	public fun observe(action: Observe.() -> Unit) {
		Observe().apply(action).complete()?.let { observers += it }
	}


	internal fun toFactory() =
		DefaultTransactionFactory(
			configurations = configurations.toList(),
			observers = observers.toList(),
		)


	override fun toString(): String = "transaction"


	public class Observe {

		private var failAction: (suspend RaptorTransactionScope.(error: Throwable) -> Unit)? = null
		private var startAction: (suspend RaptorTransactionScope.() -> Unit)? = null
		private var stopAction: (suspend RaptorTransactionScope.() -> Unit)? = null


		internal fun complete() =
			RaptorTransactionObserver(onFail = failAction, onStart = startAction, onStop = stopAction)


		@RaptorDsl
		public fun onFail(action: suspend RaptorTransactionScope.(cause: Throwable) -> Unit) {
			check(failAction == null) { "Cannot define multiple fail actions." }

			failAction = action
		}


		@RaptorDsl
		public fun onStart(action: suspend RaptorTransactionScope.() -> Unit) {
			check(failAction == null) { "Cannot define multiple start actions." }

			startAction = action
		}


		@RaptorDsl
		public fun onStop(action: suspend RaptorTransactionScope.() -> Unit) {
			check(failAction == null) { "Cannot define multiple stop actions." }

			stopAction = action
		}
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorTransactionsComponent>.onCreate(action: RaptorTransactionConfigurationScope.() -> Unit) {
	each {
		onCreate(action)
	}
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorTransactionsComponent>.observe(action: RaptorTransactionsComponent.Observe.() -> Unit) {
	each {
		observe(action)
	}
}


// FIXME (includeNested = false)
@RaptorDsl
public val RaptorTopLevelConfigurationScope.transactions: RaptorTransactionsComponent
	get() = componentRegistry.oneOrNull(Keys.transactionsComponent) ?: throw RaptorFeatureNotInstalledException(RaptorTransactionFeature)
