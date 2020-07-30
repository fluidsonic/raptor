import com.mongodb.*
import io.fluidsonic.raptor.*
import kotlin.test.*


class MongoDBTests {

	@Test
	fun testDefaultCodecs() {
		val raptor = raptor {
			install(BsonRaptorFeature) {
				includeMongoClientDefaultCodecs()
			}
		}

		val configuration = raptor.context.bsonConfiguration

		assertEquals(expected = listOf(MongoClientSettings.getDefaultCodecRegistry()), actual = configuration.registries)
	}
}
