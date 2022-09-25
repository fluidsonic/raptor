package io.fluidsonic.raptor


internal class RaptorPluginConfigurator(
	private val completionScope: RaptorAssemblyCompletionScope,
	private val componentRegistry: RaptorComponentRegistry,
	private val installationScope: RaptorAssemblyInstallationScope,
	private val plugin: RaptorPlugin,
) {

	private var notInstalledException: RaptorPluginNotInstalledException? = null
	private var pendingActions: MutableList<() -> Unit>? = null
	private val scopes = Scopes()
	private var state = State.requested


	fun configure(required: Boolean, action: () -> Unit) {
		when (state) {
			State.requested -> {
				if (required && notInstalledException == null)
					notInstalledException = RaptorPluginNotInstalledException(plugin)

				(pendingActions ?: mutableListOf<() -> Unit>().also { pendingActions = it })
					.add(action)
			}

			State.installed -> action()
			State.completed -> error("Cannot configure a completed plugin.")
		}
	}


	private fun applyPendingActions() {
		val actions = pendingActions ?: return
		pendingActions = null

		for (action in actions)
			action()
	}


	fun complete() {
		when (state) {
			State.requested -> {
				state = State.completed

				notInstalledException?.let { throw it }
			}

			State.installed -> {
				state = State.completed

				with(plugin) {
					scopes.complete()
				}
			}

			State.completed -> error("Cannot complete a plugin multiple times.")
		}
	}


	fun install() {
		when (state) {
			State.requested -> {
				notInstalledException = null
				state = State.installed

				with(plugin) {
					scopes.install()
				}

				applyPendingActions()
			}

			State.installed -> error("Cannot install a plugin multiple times.")
			State.completed -> error("Cannot install a completed plugin.")
		}
	}


	private inner class Scopes :
		RaptorPluginCompletionScope,
		RaptorPluginInstallationScope,
		RaptorAssemblyCompletionScope by completionScope,
		RaptorAssemblyInstallationScope by installationScope {

		override fun <Plugin : RaptorPlugin> complete(plugin: Plugin, action: RaptorPluginScope<Plugin>.() -> Unit) {
			// FIXME
			TODO()
		}


		override val componentRegistry: RaptorComponentRegistry
			get() = this@RaptorPluginConfigurator.componentRegistry
	}


	private enum class State {

		requested,
		installed,
		completed,
	}
}
