package io.fluidsonic.raptor.graph

import kotlin.reflect.*


internal fun KType.defaultGraphName() =
	when (val classifier = classifier) {
		is KClass<*> -> classifier.simpleName
		else -> error("Cannot derive name from Kotlin type '$this'. It must be defined explicitly: raptor…Definition(name = …)")
	}
