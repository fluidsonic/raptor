package io.fluidsonic.raptor

import java.security.*
import java.security.spec.*
import java.util.*
import javax.crypto.*
import javax.crypto.spec.*
import kotlin.experimental.*


// initially from https://crackstation.net/hashing-security.htm
@Suppress("PrivatePropertyName")
public class PasswordHasher(
	private val hashLength: Int = 18,
	private val iterationCount: Int = 64000,
	private val saltLength: Int = 24,
) {

	private val HASH_SECTIONS = 5
	private val HASH_ALGORITHM_INDEX = 0
	private val ITERATION_INDEX = 1
	private val HASH_SIZE_INDEX = 2
	private val SALT_INDEX = 3
	private val PBKDF2_INDEX = 4

	private val algorithmId = "sha512"

	private val algorithms = mapOf(
		"sha512" to "PBKDF2WithHmacSHA512"
	)


	init {
		require(hashLength >= 1) { "hash length must be at least 1" }
		require(iterationCount >= 1) { "iteration count must be at least 1" }
		require(saltLength >= 1) { "salt length must be at least 1" }
	}


	public fun createHash(password: String): String =
		createHash(password.toCharArray())


	private fun createHash(password: CharArray): String {
		val random = SecureRandom()
		val salt = ByteArray(saltLength)
		random.nextBytes(salt)

		val hash = pbkdf2(
			password = password,
			salt = salt,
			iterations = iterationCount,
			bytes = hashLength,
			algorithmName = algorithms.getValue(algorithmId)
		)
		val hashSize = hash.size

		return "$algorithmId:$iterationCount:$hashSize:${toBase64(salt)}:${toBase64(hash)}"
	}


	public fun verifyPassword(password: String, expectedHash: String): Boolean =
		verifyPassword(password = password.toCharArray(), expectedHash = expectedHash)


	private fun verifyPassword(password: CharArray, expectedHash: String): Boolean {
		val params = expectedHash.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
		if (params.size != HASH_SECTIONS)
			throw InvalidHashException("Fields are missing from the password hash.")

		val algorithmName = params[HASH_ALGORITHM_INDEX].let { algorithms[it] ?: throw CannotPerformOperationException("Unsupported hash type: $it") }

		val iterations = try {
			Integer.parseInt(params[ITERATION_INDEX])
		}
		catch (e: NumberFormatException) {
			throw InvalidHashException("Could not parse the iteration count as an integer.", cause = e)
		}

		if (iterations < 1) {
			throw InvalidHashException("Invalid number of iterations. Must be >= 1.")
		}

		val salt = try {
			fromBase64(params[SALT_INDEX])
		}
		catch (e: IllegalArgumentException) {
			throw InvalidHashException("Base64 decoding of salt failed.", cause = e)
		}

		val hash = try {
			fromBase64(params[PBKDF2_INDEX])
		}
		catch (e: IllegalArgumentException) {
			throw InvalidHashException("Base64 decoding of pbkdf2 output failed.", cause = e)
		}

		val storedHashSize = try {
			Integer.parseInt(params[HASH_SIZE_INDEX])
		}
		catch (e: NumberFormatException) {
			throw InvalidHashException("Could not parse the hash size as an integer.", cause = e)
		}

		if (storedHashSize != hash.size) {
			throw InvalidHashException("Hash length doesn't match stored hash length.")
		}

		val testHash = pbkdf2(
			password = password,
			salt = salt,
			iterations = iterations,
			bytes = hash.size,
			algorithmName = algorithmName
		)

		return slowEquals(hash, testHash)
	}


	private fun slowEquals(a: ByteArray, b: ByteArray): Boolean {
		var diff = a.size xor b.size
		var i = 0
		while (i < a.size && i < b.size) {
			diff = diff or (a[i] xor b[i]).toInt()
			i++
		}
		return diff == 0
	}


	private fun pbkdf2(password: CharArray, salt: ByteArray, iterations: Int, bytes: Int, algorithmName: String): ByteArray {
		try {
			val spec = PBEKeySpec(password, salt, iterations, bytes * 8)
			val skf = SecretKeyFactory.getInstance(algorithmName)
			return skf.generateSecret(spec).encoded
		}
		catch (e: NoSuchAlgorithmException) {
			throw CannotPerformOperationException("Hash algorithm not supported.", cause = e)
		}
		catch (e: InvalidKeySpecException) {
			throw CannotPerformOperationException("Invalid key spec.", cause = e)
		}
	}


	private fun fromBase64(hex: String): ByteArray =
		Base64.getDecoder().decode(hex)


	private fun toBase64(array: ByteArray): String =
		Base64.getEncoder().encodeToString(array)


	private class InvalidHashException(
		message: String,
		cause: Throwable? = null,
	) : Exception(message, cause)


	private class CannotPerformOperationException(
		message: String,
		cause: Throwable? = null,
	) : Exception(message, cause)
}
