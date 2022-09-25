package tests

import com.mongodb.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import io.fluidsonic.raptor.mongo.*
import kotlin.test.*


class MongoDBTests {

	@Test
	fun testDefaultCodecs() {
		val raptor = raptor {
			install(RaptorBsonPlugin)

			bson.includeMongoClientDefaultCodecs()
		}

		val bson = raptor.context.bson

		assertEquals(
			expected = listOf(RaptorBsonDefinition.of(MongoClientSettings.getDefaultCodecRegistry())),
			actual = bson.definitions,
		)
	}
}
