package io.fluidsonic.raptor.domain

import kotlin.reflect.*
import kotlin.reflect.full.*


/**
 * Fans out aggregate/projection events to registered hooks, honoring each hook's optional
 * [RaptorDomainStreamHook.aggregateIdClassFilter] / [RaptorDomainStreamHook.projectionIdClassFilter].
 *
 * Filters are resolved once at construction — see [RaptorDomainStreamHook.aggregateIdClassFilter] for the
 * exact semantics — so dispatch only ever needs a `Set.contains` check.
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


	fun dispatchAggregateEvent(event: RaptorAggregateEvent<*, *>) {
		if (!hasAggregateFilter) {
			for (hook in hooks)
				hook.onAggregateEvent(event)

			return
		}

		val aggregateIdClass = event.aggregateId::class

		for (index in hooks.indices) {
			val filter = resolvedAggregateIdClassFilters[index]
			if (filter == null || filter.contains(aggregateIdClass))
				hooks[index].onAggregateEvent(event)
		}
	}


	fun dispatchAggregateProjectionEvent(event: RaptorAggregateProjectionEvent<*, *, *>) {
		if (!hasProjectionFilter) {
			for (hook in hooks)
				hook.onAggregateProjectionEvent(event)

			return
		}

		val projectionIdClass = event.projectionId::class

		for (index in hooks.indices) {
			val filter = resolvedProjectionIdClassFilters[index]
			if (filter == null || filter.contains(projectionIdClass))
				hooks[index].onAggregateProjectionEvent(event)
		}
	}


	fun dispatchReplayCompleted() {
		for (hook in hooks)
			hook.onReplayCompleted()
	}
}
