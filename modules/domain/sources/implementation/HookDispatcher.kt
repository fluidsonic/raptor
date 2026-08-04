package io.fluidsonic.raptor.domain

import kotlin.reflect.*
import kotlin.reflect.full.*


/**
 * Fans out aggregate/projection stream messages to registered hooks, honoring each hook's optional
 * [RaptorDomainStreamHook.aggregateIdClassFilter] / [RaptorDomainStreamHook.projectionIdClassFilter].
 *
 * Filters are resolved once at construction — see [RaptorDomainStreamHook.aggregateIdClassFilter] for the
 * exact semantics — so steady-state and cold-replay dispatch only ever need a `Set.contains` check.
 */
internal class HookDispatcher(
	private val hooks: List<RaptorDomainStreamHook>,
	definitions: RaptorAggregateDefinitions,
) {

	// Registered ID classes, split by whether the aggregate is individual — individual aggregates are never
	// dispatched to hooks, so a filter matching only those must fail validation with a distinct message.
	private val registeredAggregateIdClasses: Set<KClass<out RaptorAggregateId>> =
		definitions.filterNot { it.isIndividual }.mapTo(hashSetOf()) { it.idClass }

	private val registeredIndividualAggregateIdClasses: Set<KClass<out RaptorAggregateId>> =
		definitions.filter { it.isIndividual }.mapTo(hashSetOf()) { it.idClass }

	private val registeredProjectionIdClasses: Set<KClass<out RaptorAggregateProjectionId>> =
		definitions.mapNotNull { it.projectionDefinition }.mapTo(hashSetOf()) { it.idClass }

	// Resolved once at construction, index-aligned with `hooks` (never keyed by the hook itself — a hook
	// type is free to define structural equals()/hashCode(), which would collapse distinct hooks sharing
	// an equality class into one map entry and silently apply one hook's filter to the other).
	// null means the hook declared no filter (everything); a present set contains only exact registered
	// ID classes, already expanded from whatever the hook declared.
	private val resolvedAggregateIdClassFilters: List<Set<KClass<out RaptorAggregateId>>?> =
		hooks.map(::resolveAggregateIdClassFilter)

	private val resolvedProjectionIdClassFilters: List<Set<KClass<out RaptorAggregateProjectionId>>?> =
		hooks.map(::resolveProjectionIdClassFilter)

	private val hasAggregateFilter: Boolean = resolvedAggregateIdClassFilters.any { it != null }
	private val hasProjectionFilter: Boolean = resolvedProjectionIdClassFilters.any { it != null }


	private fun resolveAggregateIdClassFilter(hook: RaptorDomainStreamHook): Set<KClass<out RaptorAggregateId>>? {
		val declaredClasses = hook.aggregateIdClassFilter ?: return null
		val resolvedClasses = hashSetOf<KClass<out RaptorAggregateId>>()

		for (declaredClass in declaredClasses) {
			val matches = registeredAggregateIdClasses.filter { declaredClass.isSuperclassOf(it) }
			if (matches.isNotEmpty()) {
				resolvedClasses += matches

				continue
			}

			if (registeredIndividualAggregateIdClasses.any { declaredClass.isSuperclassOf(it) })
				error(
					"Hook ${hook::class.qualifiedName} declared 'aggregateIdClassFilter' class ${declaredClass.qualifiedName}, " +
						"but that class matches only individual aggregates, which are never dispatched to hooks."
				)

			error(
				"Hook ${hook::class.qualifiedName} declared 'aggregateIdClassFilter' class ${declaredClass.qualifiedName}, " +
					"but no registered aggregate has a matching ID class."
			)
		}

		return resolvedClasses
	}


	private fun resolveProjectionIdClassFilter(hook: RaptorDomainStreamHook): Set<KClass<out RaptorAggregateProjectionId>>? {
		val declaredClasses = hook.projectionIdClassFilter ?: return null
		val resolvedClasses = hashSetOf<KClass<out RaptorAggregateProjectionId>>()

		for (declaredClass in declaredClasses) {
			val matches = registeredProjectionIdClasses.filter { declaredClass.isSuperclassOf(it) }
			if (matches.isEmpty())
				error(
					"Hook ${hook::class.qualifiedName} declared 'projectionIdClassFilter' class ${declaredClass.qualifiedName}, " +
						"but no registered aggregate projection has a matching ID class."
				)

			resolvedClasses += matches
		}

		return resolvedClasses
	}


	// Steady-state dispatch (one commit's worth of batches). Deliberately a single loop with per-hook
	// aggregate-then-projection interleaving, matching each hook's own call order — not two separate loops.
	fun dispatchLive(batch: RaptorAggregateEventBatch<*, *>, projectionBatch: RaptorAggregateProjectionEventBatch<*, *, *>?) {
		val aggregateIdClass = batch.aggregateId::class
		val projectionIdClass = projectionBatch?.projectionId?.let { it::class }

		for (index in hooks.indices) {
			val hook = hooks[index]

			if (!hasAggregateFilter || resolvedAggregateIdClassFilters[index]?.contains(aggregateIdClass) != false)
				hook.onAggregateStreamMessage(batch)

			if (projectionBatch != null && projectionIdClass != null)
				if (!hasProjectionFilter || resolvedProjectionIdClassFilters[index]?.contains(projectionIdClass) != false)
					hook.onAggregateProjectionStreamMessage(projectionBatch)
		}
	}


	// Cold-replay dispatch. `batchAggregateIdClasses`/`projectionBatchIdClasses` are parallel to
	// `batch.batches`/`projectionBatch?.batches` (same index), sourced from stored KClasses — zero
	// `::class` calls across the whole replay. Never sorted: batches are already in construction order,
	// which is the only order that's correct (a batch can span non-contiguous event ids).
	fun dispatchReplay(
		batch: RaptorAggregateStreamMessage.Replay,
		batchAggregateIdClasses: List<KClass<out RaptorAggregateId>>,
		projectionBatch: RaptorAggregateProjectionStreamMessage.Replay?,
		projectionBatchIdClasses: List<KClass<out RaptorAggregateProjectionId>>,
	) {
		if (!hasAggregateFilter && !hasProjectionFilter) {
			for (hook in hooks) {
				hook.onAggregateStreamMessage(batch)

				if (projectionBatch != null)
					hook.onAggregateProjectionStreamMessage(projectionBatch)
			}

			return
		}

		val batches = batch.batches
		val projectionBatches = projectionBatch?.batches.orEmpty()

		val batchesByIdClass = if (hasAggregateFilter) groupByIndex(batches, batchAggregateIdClasses) else emptyMap()
		val projectionBatchesByIdClass =
			if (hasProjectionFilter) groupByIndex(projectionBatches, projectionBatchIdClasses) else emptyMap()
		val assembledBatchesByFilter = hashMapOf<Set<KClass<out RaptorAggregateId>>, List<RaptorAggregateEventBatch<*, *>>>()
		val assembledProjectionBatchesByFilter =
			hashMapOf<Set<KClass<out RaptorAggregateProjectionId>>, List<RaptorAggregateProjectionEventBatch<*, *, *>>>()

		for (index in hooks.indices) {
			val hook = hooks[index]
			val aggregateFilter = resolvedAggregateIdClassFilters[index]
			val hookBatches = when {
				aggregateFilter == null -> batches
				aggregateFilter.isEmpty() -> null
				aggregateFilter.size == 1 -> batchesByIdClass[aggregateFilter.single()]
				else -> assembledBatchesByFilter.getOrPut(aggregateFilter) {
					batches.filterIndexed { i, _ -> batchAggregateIdClasses[i] in aggregateFilter }
				}
			}

			if (!hookBatches.isNullOrEmpty())
				hook.onAggregateStreamMessage(RaptorAggregateStreamMessage.Replay(hookBatches))

			if (projectionBatch != null) {
				val projectionFilter = resolvedProjectionIdClassFilters[index]
				val hookProjectionBatches = when {
					projectionFilter == null -> projectionBatches
					projectionFilter.isEmpty() -> null
					projectionFilter.size == 1 -> projectionBatchesByIdClass[projectionFilter.single()]
					else -> assembledProjectionBatchesByFilter.getOrPut(projectionFilter) {
						projectionBatches.filterIndexed { i, _ -> projectionBatchIdClasses[i] in projectionFilter }
					}
				}

				if (!hookProjectionBatches.isNullOrEmpty())
					hook.onAggregateProjectionStreamMessage(RaptorAggregateProjectionStreamMessage.Replay(hookProjectionBatches))
			}
		}
	}


	fun dispatchLoaded() {
		for (hook in hooks) {
			hook.onAggregateStreamMessage(RaptorAggregateStreamMessage.Loaded)
			hook.onAggregateProjectionStreamMessage(RaptorAggregateProjectionStreamMessage.Loaded)
		}
	}
}


private fun <Key, Value> groupByIndex(values: List<Value>, keys: List<Key>): Map<Key, List<Value>> {
	val result: MutableMap<Key, MutableList<Value>> = hashMapOf()

	for (index in values.indices)
		result.getOrPut(keys[index], ::mutableListOf) += values[index]

	return result
}
