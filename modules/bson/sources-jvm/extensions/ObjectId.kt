package io.fluidsonic.raptor.bson

import org.bson.types.*


@Suppress("FunctionName")
public fun ObjectIdOrNull(value: String): ObjectId? =
	try {
		ObjectId(value)
	}
	catch (e: IllegalArgumentException) {
		null
	}
