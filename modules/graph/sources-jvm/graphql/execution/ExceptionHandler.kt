package io.fluidsonic.raptor.graph

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.di.*
import io.fluidsonic.raptor.transactions.*
import kotlin.reflect.*
import org.slf4j.*


// FIXME Rework exception handling.
internal class ExceptionHandler(
	handlers: Collection<GraphExceptionHandler<*>>,
) : GExceptionHandler {

	private val handlers: Map<KClass<out Throwable>, GraphExceptionHandler<*>> = handlers.associateByTo(hashMapOf()) { it.exceptionClass }


	private fun handler(exception: Throwable): GraphExceptionHandler<*>? {
		val exceptionClass = exception::class
		handlers[exceptionClass]?.let { return it }

		val matches = handlers.keys.filter { it.isInstance(exception) }

		return when (matches.size) {
			0 -> null
			1 -> handlers[matches.single()]
			else -> handlers[closest(matches, exceptionClass)]
		}
	}


	// TODO add origin/locations/nodes
	override fun GExceptionHandlerContext.handleException(exception: Throwable): GError =
		checkNotNull(origin.context.execution.raptorContext).handleException(exception, origin = origin)


	private fun RaptorTransactionContext.handleException(exception: Throwable, origin: GExceptionOrigin, depth: Int = 1): GError {
		if (exception is GErrorException)
			throw exception

		handler(exception)?.let { handler ->
			try {
				@Suppress("UNCHECKED_CAST")
				with(handler as GraphExceptionHandler<Throwable>) {
					return handle(exception).let { error ->
						GError(message = error.message, extensions = error.extensions)
					}
				}
			}
			catch (e: Throwable) {
				if (e !== exception) {
					if (e !== exception.cause && exception.suppressed.none { it === e })
						e.addSuppressed(exception)

					when (e::class) {
						exception::class -> throw e
						else -> when {
							depth >= 100 -> throw RuntimeException("GraphQL exception handlers caused a cycle.", e)
							else -> return handleException(e, origin = origin, depth = depth + 1)
						}
					}
				}
			}
		}

		when (val logger = context.di.getOrNull<Logger>()) {
			null -> {
				System.err.println("Unhandled GraphQL exception.")
				exception.printStackTrace()
			}

			else -> logger.error("Unhandled GraphQL exception at '${origin.path}'.", exception)
		}

		return internalError
	}


	companion object {

		private val internalError = GError(
			message = "An internal error occurred.",
			extensions = mapOf("code" to "internal"),
		)
	}
}


private fun closest(parents: Collection<KClass<out Throwable>>, child: KClass<out Throwable>): KClass<out Throwable> {
	fun distance(parent: Class<*>, child: Class<*>): Int {
		var distance = 0

		var current = child
		while (current != parent) {
			current = current.superclass
			++distance
		}

		return distance
	}


	val childClass = child.java

	return parents.minBy { distance(it.java, childClass) }
}
