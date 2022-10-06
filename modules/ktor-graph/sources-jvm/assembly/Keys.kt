package io.fluidsonic.raptor.ktor.graph

import io.fluidsonic.graphql.*
import io.fluidsonic.raptor.*


internal object Keys {

	val graphInstalledExtension = RaptorComponentExtensionKey<Boolean>("ktor graph installed")
	val graphRouteProperty = RaptorPropertyKey<GraphRoute>("ktor graph route")
	val graphSchemaProperty = RaptorPropertyKey<GSchema>("ktor graph schema")
}
