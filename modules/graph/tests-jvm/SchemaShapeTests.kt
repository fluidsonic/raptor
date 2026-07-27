package tests

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.graph.*
import kotlin.test.*


/**
 * The five built-in GraphQL scalars must be fluid's own singletons — raptor must neither shadow
 * them with custom scalar types nor re-declare them in the generated SDL.
 */
class SchemaShapeTests {

	// `GSchema` lists a built-in scalar only when a type or directive definition refers to it, so the
	// schema under test must name all five for the assertions below to be meaningful.
	private val fixture = graphFixture {
		definitions.includeDefault()

		definitions.newIdAlias<ThingId> {
			parse { ThingId(it) }
			serialize { it.value }
		}

		definitions.add(
			graphOperationDefinition<String>(name = "hello", operationType = RaptorGraphOperationType.query) {
				resolver { "world" }
			},
			graphOperationDefinition<Double>(name = "ratio", operationType = RaptorGraphOperationType.query) {
				resolver { 0.5 }
			},
			graphOperationDefinition<Int>(name = "count", operationType = RaptorGraphOperationType.query) {
				resolver { 1 }
			},
			graphOperationDefinition<ThingId>(name = "thingId", operationType = RaptorGraphOperationType.query) {
				resolver { ThingId("thing-1") }
			},
		)
	}

	private val schema: GSchema
		get() = fixture.graph.schema

	private val sdlLines: List<String>
		get() = printSchema(schema, indent = "\t").lines()


	@Test
	fun booleanResolvesToBuiltInType() {
		val type = schema.resolveType("Boolean")
		assertIs<GBooleanType>(type)
		assertFalse(type is GCustomScalarType, "'Boolean' must not be a custom scalar type")
	}


	@Test
	fun floatResolvesToBuiltInType() {
		val type = schema.resolveType("Float")
		assertIs<GFloatType>(type)
		assertFalse(type is GCustomScalarType, "'Float' must not be a custom scalar type")
	}


	@Test
	fun idResolvesToBuiltInType() {
		val type = schema.resolveType("ID")
		assertIs<GIdType>(type)
		assertFalse(type is GCustomScalarType, "'ID' must not be a custom scalar type")
	}


	@Test
	fun intResolvesToBuiltInType() {
		val type = schema.resolveType("Int")
		assertIs<GIntType>(type)
		assertFalse(type is GCustomScalarType, "'Int' must not be a custom scalar type")
	}


	@Test
	fun stringResolvesToBuiltInType() {
		val type = schema.resolveType("String")
		assertIs<GStringType>(type)
		assertFalse(type is GCustomScalarType, "'String' must not be a custom scalar type")
	}


	// Raptor maps Kotlin types like `Boolean` and `Int` onto the built-in scalar names, and `GSchema`
	// drops those mappings in favour of fluid's own singletons. This asserts the type list carries no
	// leftover custom scalar under a built-in name — which would shadow the singleton by-name lookups
	// asserted above.
	@Test
	fun schemaDeclaresNoCustomScalarsForBuiltInNames() {
		val builtInNames = listOf("Boolean", "Float", "ID", "Int", "String")
		val offenders = schema.types.filterIsInstance<GCustomScalarType>().filter { it.name in builtInNames }

		assertEquals(actual = offenders.map { it.name }, expected = emptyList())
	}


	@Test
	fun sdlOmitsBooleanScalarDeclaration() {
		assertSdlOmits("scalar Boolean")
	}


	@Test
	fun sdlOmitsFloatScalarDeclaration() {
		assertSdlOmits("scalar Float")
	}


	@Test
	fun sdlOmitsIdScalarDeclaration() {
		assertSdlOmits("scalar ID")
	}


	@Test
	fun sdlOmitsIntScalarDeclaration() {
		assertSdlOmits("scalar Int")
	}


	@Test
	fun sdlOmitsStringScalarDeclaration() {
		assertSdlOmits("scalar String")
	}


	@Test
	fun sdlRetainsNonBuiltInTypeDeclarations() {
		assertSdlContains("scalar CountryCode")
		assertSdlContains("scalar Currency")
		assertSdlContains("scalar Timestamp")
		assertSdlContains("scalar Unit")
		assertSdlContains("type Country {")
	}


	// The built-in scalar names resolve through a lookup that carries no raptor type, since raptor emits no
	// definition for them. A Kotlin type with no mapping at all must still be rejected by raptor's own
	// diagnostic rather than pass through that lookup unnoticed. An object field is the vehicle because a
	// missing mapping on an operation's own output type is caught one stage earlier.
	@Test
	fun unmappedKotlinTypeIsRejected() {
		val exception = assertNotNull(
			runCatching {
				graphFixture {
					definitions.add(
						graphObjectDefinition<Thing> {
							field(Thing::unmapped)
						},
						graphOperationDefinition<Thing>(name = "thing", operationType = RaptorGraphOperationType.query) {
							resolver { Thing(Unmapped) }
						},
					)
				}
			}.exceptionOrNull(),
			"expected assembly to fail for a Kotlin type without a GraphQL mapping",
		)

		assertIs<IllegalStateException>(exception, "expected raptor's own error but got $exception")
		assertContains(exception.message.orEmpty(), "Cannot resolve GraphQL type for Kotlin type")
		assertContains(exception.message.orEmpty(), "Unmapped")
	}


	private fun assertSdlContains(line: String) {
		assertTrue(sdlLines.contains(line), "expected SDL to contain the line '$line':\n$schema")
	}


	private fun assertSdlOmits(line: String) {
		assertFalse(sdlLines.contains(line), "expected SDL to not contain the line '$line':\n$schema")
	}
}


@JvmInline
private value class ThingId(val value: String)


private class Thing(
	val unmapped: Unmapped,
)


private object Unmapped
