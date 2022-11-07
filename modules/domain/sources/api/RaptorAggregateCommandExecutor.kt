package io.fluidsonic.raptor.domain

import kotlin.contracts.*


public interface RaptorAggregateCommandExecutor {

	public suspend fun execution(): RaptorAggregateCommandExecution
}


public suspend fun <Id : RaptorAggregateId> RaptorAggregateCommandExecutor.execute(
	id: Id,
	command: RaptorAggregateCommand<Id>,
) {
	execute(id = id, version = null, command = command)
}


public suspend fun <Id : RaptorAggregateId> RaptorAggregateCommandExecutor.execute(
	id: Id,
	version: Int?,
	command: RaptorAggregateCommand<Id>,
) {
	execution(retryOnVersionConflict = (version == null)) {
		execute(id = id, version = version, command = command)
	}
}


public suspend inline fun <Result> RaptorAggregateCommandExecutor.execution(
	retryOnVersionConflict: Boolean = false,
	action: RaptorAggregateCommandExecution.() -> Result,
): Result {
	contract {
		callsInPlace(action, InvocationKind.AT_LEAST_ONCE)
	}

	repeat(100) {
		try {
			return execution().let { execution ->
				execution.run(action).also {
					execution.commit()
				}
			}
		}
		catch (e: RaptorAggregateVersionConflict) {
			if (!retryOnVersionConflict)
				throw e
		}
	}

	error(
		"Command execution was repeated 100 times due to version conflicts.\n" +
			"Did you forget to make sure that the expected aggregate version is (re)loaded within the batch action?"
	)
}
