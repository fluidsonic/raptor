# Two-phase plugin assembly (install then complete)

The core assembly builds a Raptor in two strictly separated passes. Knowing which pass
does what is essential when wiring any plugin.

`RaptorAssembly` runs an inner `Installation` object (user configure block + plugin
installs) whose result feeds a separate inner `Completion` object (finalize plugins into
configurations). Both phases define their *own* private inner class literally named
`PluginConfigurator` with different responsibilities — a grep for `PluginConfigurator`
hits two unrelated types. Anchors: `RaptorAssembly.kt` (`Installation`, `Completion`).

Key behaviors:
- **`install()` is idempotent by instance identity** (not class) and re-entrant-safe.
  `require(Plugin)` does not fail immediately when absent — it stores a
  `RaptorPluginNotInstalledException` and queues the action; `install()` later nulls that
  stored exception and flushes pending actions. So you may `require` a plugin before it is
  installed; the deferred exception only throws at completion if it was never installed.
- **Completion is dependent-first.** `Completion.PluginConfigurator.complete()` completes
  a plugin's `dependents` before the plugin itself, guarding only against direct
  self-re-entry (`check(!isCompleting)` → "Plugin completion cycle detected"). Real cycle
  detection is a documented TODO; re-entry is tolerated.
- **Register BSON/DI/etc. contributions in `complete()`, not `install()`**, when they
  depend on the finalized set of definitions (e.g. `RaptorDomainMongoPlugin.complete()`
  reads all aggregate definitions before registering their codecs).
- Plugin configurations are wrapped in `DefaultRaptorPluginConfigurationSet` and stored
  under `Keys.pluginConfigurationSet` in the property registry — the bridge that makes
  runtime `RaptorPluginConfigurationsSet.get(plugin)` work.

`RaptorPlugin` = `RaptorPluginWithConfiguration<Unit>` with a no-op `complete()`.
`RaptorFeature`/`RaptorFeatureScope` are deprecated aliases (the API was renamed from
"Feature" to "Plugin"). See also `assembly/component-model.md`, `assembly/registries.md`.
