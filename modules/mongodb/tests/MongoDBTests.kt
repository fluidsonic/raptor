package tests

import com.mongodb.*
import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.bson.*
import kotlin.test.*


class MongoDBTests {

	@Test
	fun testDefaultCodecs() {
		val raptor = raptor {
			install(RaptorBsonFeature) {
				includeMongoClientDefaultCodecs()
			}
		}

		val configuration = raptor.context.bsonConfiguration

		assertEquals(
			expected = RaptorBsonDefinition.of(MongoClientSettings.getDefaultCodecRegistry()),
			actual = configuration.definitions
		)
	}
}
