package io.fluidsonic.raptor.bson

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*


private val bsonComponentKey = RaptorComponentKey<RaptorBsonComponent>("bson")


public object RaptorBsonPlugin : RaptorPlugin {

	override fun RaptorPluginInstallationScope.install() {
		componentRegistry.register(bsonComponentKey, RaptorBsonComponent())

		optional(RaptorDIPlugin) {
			di {
				provide { get<RaptorBson>().codecRegistry }
				provide { get<RaptorBson>().scope }
				provide { get<RaptorContext>().bson }
			}
		}
	}


	override fun toString(): String = "bson"
}


@RaptorDsl
public val RaptorPluginScope<in RaptorBsonPlugin>.bson: RaptorBsonComponent
	get() = componentRegistry.oneOrNull(bsonComponentKey) ?: throw RaptorPluginNotInstalledException(RaptorBsonPlugin)
