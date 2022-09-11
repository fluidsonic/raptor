package tests

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.graph.*
import io.fluidsonic.raptor.ktor.*
import io.fluidsonic.raptor.ktor.graph.*
import kotlin.test.*


class AssemblyTests {

	@Test
	fun testSchemaRoute() {
		val raptor = raptor {
			install(RaptorGraphFeature)
			install(RaptorKtorFeature)

			graphs.new()

			ktor.servers.new {
				routes.new("/schema").graphSchema()
			}

			ktor.servers.new().routes.new("/schemaB") {
				graphSchema()
			}
		}
	}
}
