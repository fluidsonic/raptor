package io.fluidsonic.raptor.mongo2

import com.mongodb.client.model.*
import com.mongodb.client.model.geojson.*
import com.mongodb.lang.*
import java.util.regex.*
import org.bson.*
import org.bson.conversions.*


public object MongoFilters {

	public fun eq(value: Any?): Bson = Filters.eq(value)
	public fun eq(fieldName: String, value: Any?): Bson = Filters.eq(fieldName, value)
	public fun ne(fieldName: String, value: Any?): Bson = Filters.ne(fieldName, value)
	public fun gt(fieldName: String, value: Any): Bson = Filters.gt(fieldName, value)
	public fun lt(fieldName: String, value: Any): Bson = Filters.lt(fieldName, value)
	public fun gte(fieldName: String, value: Any): Bson = Filters.gte(fieldName, value)
	public fun lte(fieldName: String, value: Any): Bson = Filters.lte(fieldName, value)

	@SafeVarargs
	public fun `in`(fieldName: String, vararg values: Any?): Bson = Filters.`in`(fieldName, *values)

	public fun `in`(fieldName: String, values: Iterable<Any?>): Bson = Filters.`in`(fieldName, values)
	public fun nin(fieldName: String, values: Iterable<Any?>): Bson = Filters.nin(fieldName, values)

	@SafeVarargs
	public fun nin(fieldName: String, vararg values: Any?): Bson = Filters.nin(fieldName, *values)

	public fun and(filters: Iterable<Bson>): Bson = Filters.and(filters)

	@SafeVarargs
	public fun and(vararg filters: Bson): Bson = Filters.and(*filters)

	public fun or(filters: Iterable<Bson>): Bson = Filters.or(filters)

	@SafeVarargs
	public fun or(vararg filters: Bson): Bson = Filters.or(*filters)

	public fun not(filter: Bson): Bson = Filters.not(filter)
	public fun nor(filters: Iterable<Bson>): Bson = Filters.nor(filters)

	@SafeVarargs
	public fun nor(vararg filters: Bson): Bson = Filters.nor(*filters)

	public fun exists(fieldName: String, exists: Boolean = true): Bson = Filters.exists(fieldName, exists)
	public fun type(fieldName: String, type: BsonType): Bson = Filters.type(fieldName, type)
	public fun type(fieldName: String, type: String): Bson = Filters.type(fieldName, type)
	public fun mod(fieldName: String, divisor: Long, remainder: Long): Bson = Filters.mod(fieldName, divisor, remainder)
	public fun regex(fieldName: String, pattern: String, @Nullable options: String?): Bson = Filters.regex(fieldName, pattern, options)
	public fun regex(fieldName: String, pattern: Pattern): Bson = Filters.regex(fieldName, pattern)
	public fun text(search: String): Bson = Filters.text(search)
	public fun text(search: String, textSearchOptions: TextSearchOptions): Bson = Filters.text(search, textSearchOptions)
	public fun where(javaScriptExpression: String): Bson = Filters.where(javaScriptExpression)
	public fun expr(expression: Any): Bson = Filters.expr(expression)

	@SafeVarargs
	public fun all(fieldName: String, vararg values: Any?): Bson = Filters.all(fieldName, *values)

	public fun all(fieldName: String, values: Iterable<Any?>): Bson = Filters.all(fieldName, values)
	public fun elemMatch(fieldName: String, filter: Bson): Bson = Filters.elemMatch(fieldName, filter)
	public fun size(fieldName: String, size: Int): Bson = Filters.size(fieldName, size)
	public fun bitsAllClear(fieldName: String, bitmask: Long): Bson = Filters.bitsAllClear(fieldName, bitmask)
	public fun bitsAllSet(fieldName: String, bitmask: Long): Bson = Filters.bitsAllSet(fieldName, bitmask)
	public fun bitsAnyClear(fieldName: String, bitmask: Long): Bson = Filters.bitsAnyClear(fieldName, bitmask)
	public fun bitsAnySet(fieldName: String, bitmask: Long): Bson = Filters.bitsAnySet(fieldName, bitmask)
	public fun geoWithin(fieldName: String, geometry: Geometry): Bson = Filters.geoWithin(fieldName, geometry)
	public fun geoWithin(fieldName: String, geometry: Bson): Bson = Filters.geoWithin(fieldName, geometry)

	public fun geoWithinBox(fieldName: String, lowerLeftX: Double, lowerLeftY: Double, upperRightX: Double, upperRightY: Double): Bson =
		Filters.geoWithinBox(fieldName, lowerLeftX, lowerLeftY, upperRightX, upperRightY)

	public fun geoWithinPolygon(fieldName: String, points: List<List<Double>>): Bson = Filters.geoWithinPolygon(fieldName, points)
	public fun geoWithinCenter(fieldName: String, x: Double, y: Double, radius: Double): Bson = Filters.geoWithinCenter(fieldName, x, y, radius)
	public fun geoWithinCenterSphere(fieldName: String, x: Double, y: Double, radius: Double): Bson = Filters.geoWithinCenterSphere(fieldName, x, y, radius)
	public fun geoIntersects(fieldName: String, geometry: Bson): Bson = Filters.geoIntersects(fieldName, geometry)
	public fun geoIntersects(fieldName: String, geometry: Geometry): Bson = Filters.geoIntersects(fieldName, geometry)

	public fun near(fieldName: String, geometry: Point, maxDistance: Double?, minDistance: Double?): Bson =
		Filters.near(fieldName, geometry, maxDistance, minDistance)

	public fun near(fieldName: String, geometry: Bson, maxDistance: Double?, minDistance: Double?): Bson =
		Filters.near(fieldName, geometry, maxDistance, minDistance)

	public fun near(fieldName: String, x: Double, y: Double, maxDistance: Double?, minDistance: Double?): Bson =
		Filters.near(fieldName, x, y, maxDistance, minDistance)

	public fun nearSphere(fieldName: String, geometry: Point, maxDistance: Double?, minDistance: Double?): Bson =
		Filters.nearSphere(fieldName, geometry, maxDistance, minDistance)

	public fun nearSphere(fieldName: String, geometry: Bson, maxDistance: Double?, minDistance: Double?): Bson =
		Filters.nearSphere(fieldName, geometry, maxDistance, minDistance)

	public fun nearSphere(fieldName: String, x: Double, y: Double, maxDistance: Double?, minDistance: Double?): Bson =
		Filters.nearSphere(fieldName, x, y, maxDistance, minDistance)

	public fun jsonSchema(schema: Bson): Bson =
		Filters.jsonSchema(schema)

	public fun empty(): Bson =
		Filters.empty()
}
