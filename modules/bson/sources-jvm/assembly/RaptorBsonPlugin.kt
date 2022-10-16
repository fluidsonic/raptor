package io.fluidsonic.raptor.bson

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import org.bson.codecs.configuration.*


private val bsonComponentKey = RaptorComponentKey<RaptorBsonComponent>("bson")


public object RaptorBsonPlugin : RaptorPlugin {

	override fun RaptorPluginInstallationScope.install() {
		componentRegistry.register(bsonComponentKey, RaptorBsonComponent())

		optional(RaptorDIPlugin) {
			di {
				provide<CodecRegistry> { get<RaptorBson>().codecRegistry }
				provide<RaptorBsonScope> { get<RaptorBson>().scope }
				provide<RaptorBson> { get<RaptorContext>().bson }
			}
		}
	}


	override fun toString(): String = "bson"
}


@RaptorDsl
public val RaptorPluginScope<in RaptorBsonPlugin>.bson: RaptorBsonComponent
	get() = componentRegistry.oneOrNull(bsonComponentKey) ?: throw RaptorPluginNotInstalledException(RaptorBsonPlugin)
