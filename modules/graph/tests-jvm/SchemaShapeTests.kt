package tests

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.graph.*
import kotlin.test.*


/**
 * The five built-in GraphQL scalars must be fluid's own singletons — raptor must neither shadow
 * them with custom scalar types nor re-declare them in the generated SDL.
 */
class SchemaShapeTests {

	private val fixture = graphFixture {
		definitions.includeDefault()
		definitions.add(
			graphOperationDefinition<String>(name = "hello", operationType = RaptorGraphOperationType.query) {
				resolver { "world" }
			},
		)
	}

	private val schema: GSchema
		get() = fixture.graph.schema

	private val sdlLines: List<String>
		get() = schema.toString().lines()


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


	// `GSchema` appends fluid's built-in types last, so they win the by-name lookup used by
	// `resolveType(…)` even while raptor also emits its own custom scalars for those names.
	// This asserts the underlying type list itself is free of such duplicates.
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


	private fun assertSdlContains(line: String) {
		assertTrue(sdlLines.contains(line), "expected SDL to contain the line '$line':\n$schema")
	}


	private fun assertSdlOmits(line: String) {
		assertFalse(sdlLines.contains(line), "expected SDL to not contain the line '$line':\n$schema")
	}
}
