package io.fluidsonic.raptor

import kotlin.collections.set
import kotlin.reflect.*


public class RaptorEntitiesComponent internal constructor() : RaptorComponent.Base<RaptorEntitiesComponent>(RaptorEntitiesPlugin) {

	internal val resolverTypes: MutableMap<KClass<out RaptorEntityId>, KType> = hashMapOf()


	internal companion object {

		val key = RaptorComponentKey<RaptorEntitiesComponent>("entities")
	}
}


// FIXME
@RaptorDsl
public inline fun <reified Id : RaptorEntityId, reified Resolver : RaptorEntityResolver<*, Id>> RaptorAssemblyQuery<RaptorEntitiesComponent>.resolver() {
	resolver(id = Id::class, resolverType = typeOf<Resolver>())
}


@RaptorDsl
public fun RaptorAssemblyQuery<RaptorEntitiesComponent>.resolver(id: KClass<out RaptorEntityId>, resolverType: KType) {
	this { this.resolverTypes[id] = resolverType } // FIXME check collision
}
