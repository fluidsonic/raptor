package io.fluidsonic.raptor

import io.github.cdimascio.dotenv.*


@RaptorDsl
public class RaptorEnvironment private constructor(
	private val dotenv: Dotenv = dotenv { ignoreIfMissing = true },
	private val prefix: String = "",
) {

	@Suppress("FunctionName")
	internal fun _get(key: String): String =
		resolve(key).let { fullKey ->
			dotenv[fullKey] ?: error("Required environment variable '$fullKey' is not set.")
		}


	@Suppress("FunctionName")
	internal fun _getOrNull(key: String): String? =
		dotenv[resolve(key)]


	// TODO Fail on empty key?
	public fun resolve(key: String): String =
		when {
			prefix.isEmpty() || key.isEmpty() -> key
			else -> "$prefix$key"
		}


	@RaptorDsl
	public fun scope(key: String, separator: String = "_"): RaptorEnvironment =
		when {
			key.isEmpty() && separator.isEmpty() -> this
			else -> RaptorEnvironment(dotenv = dotenv, prefix = "$prefix$key$separator")
		}


	internal companion object {

		val instance = RaptorEnvironment()
	}
}


@RaptorDsl
public operator fun RaptorEnvironment.get(key: String): String =
	_get(key)


@RaptorDsl
public fun RaptorEnvironment.getOrNull(key: String): String? =
	_getOrNull(key)


@RaptorDsl
public inline operator fun <Result> RaptorEnvironment.invoke(block: RaptorEnvironment.() -> Result): Result =
	with(this, block)


@RaptorDsl
public inline fun <Result> RaptorEnvironment.scope(key: String, separator: String = "_", block: RaptorEnvironment.() -> Result): Result =
	with(scope(key, separator = separator), block)


@RaptorDsl
@Suppress("unused")
public val RaptorGlobalDsl.environment: RaptorEnvironment
	get() = RaptorEnvironment.instance


@RaptorDsl
@Suppress("unused")
public inline fun <Result> RaptorGlobalDsl.environment(block: RaptorEnvironment.() -> Result): Result =
	with(environment, block)
