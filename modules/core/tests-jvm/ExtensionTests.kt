package tests

import io.fluidsonic.raptor.*
import kotlin.test.*


class ExtensionTests {

	@Test
	fun testExtension() {
		raptor {
			install(CounterPlugin)

			counter {
				extensions[anyComponentExtensionKey] = "foo"
			}

			install(CounterPlugin)

			counter {
				assertEquals(expected = "foo", actual = extensions[anyComponentExtensionKey])
			}
		}
	}
}
