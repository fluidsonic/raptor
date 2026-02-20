package io.fluidsonic.raptor.service2

import kotlin.test.*


/**
 * Tests for ServiceDIKey2.
 */
class ServiceDIKeyTests {

	@Test
	fun `ServiceDIKey2 has correct name`() {
		val key = ServiceDIKey2<TestService>("test-service")

		assertEquals(actual = key.name, expected = "test-service")
	}


	@Test
	fun `ServiceDIKey2 is not optional`() {
		val key = ServiceDIKey2<TestService>("test-service")

		assertFalse(key.isOptional)
	}


	@Test
	fun `ServiceDIKey2 notOptional returns self`() {
		val key = ServiceDIKey2<TestService>("test-service")

		assertSame(actual = key.notOptional(), expected = key)
	}


	@Test
	fun `ServiceDIKey2 toString includes name`() {
		val key = ServiceDIKey2<TestService>("my-service")

		assertEquals(actual = key.toString(), expected = "RaptorService2 (my-service)")
	}


	@Test
	fun `ServiceDIKey2 instances with same name are equal`() {
		val key1 = ServiceDIKey2<TestService>("test-service")
		val key2 = ServiceDIKey2<TestService>("test-service")

		assertEquals(actual = key1, expected = key2)
	}


	@Test
	fun `ServiceDIKey2 instances with different names are not equal`() {
		val key1 = ServiceDIKey2<TestService>("service-a")
		val key2 = ServiceDIKey2<TestService>("service-b")

		assertNotEquals(illegal = key2, actual = key1)
	}


}
