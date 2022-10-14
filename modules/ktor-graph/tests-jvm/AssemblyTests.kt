package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.graph.*
import io.fluidsonic.raptor.ktor.*
import io.fluidsonic.raptor.ktor.graph.*
import io.fluidsonic.raptor.lifecycle.*
import io.fluidsonic.raptor.transactions.*
import kotlin.test.*


class AssemblyTests {

	@Test
	fun testBasics() {
		val raptor = raptor {
			install(RaptorGraphPlugin)
			install(RaptorLifecyclePlugin)
			install(RaptorKtorPlugin)
			install(RaptorTransactionPlugin)

			graphs.new()

			ktor.servers.new {
				routes.new("graphql") {
					graph()

					routes.new("schema").graphSchema()
				}
			}

			ktor.servers.new {
				routes.new("graphql") {
					graph()

					routes.new("schema").graphSchema()
				}
			}
		}
	}
}
