package io.fluidsonic.raptor.keyvaluestore.mongo2

import kotlin.reflect.*
import kotlin.reflect.full.*


// FIXME
internal fun KType.withArguments(arguments: List<KTypeProjection>): KType =
	when (arguments) {
		this.arguments -> this
		else -> (classifier as KClass<*>).createType(arguments)
	}


internal fun KType.withArguments(vararg arguments: KTypeProjection): KType =
	withArguments(arguments.toList())
