package io.fluidsonic.raptor

import kotlin.collections.set
import kotlin.reflect.*


public class RaptorEntitiesComponent internal constructor() : RaptorComponent2.Base() {

	internal val resolverTypes: MutableMap<KClass<out RaptorEntityId>, KType> = hashMapOf()


	internal object Key : RaptorComponentKey2<RaptorEntitiesComponent> {

		override fun toString() = "entities"
	}
}


// FIXME
@RaptorDsl
public inline fun <reified Id : RaptorEntityId, reified Resolver : RaptorEntityResolver<*, Id>> RaptorAssemblyQuery2<RaptorEntitiesComponent>.resolver() {
	resolver(id = Id::class, resolverType = typeOf<Resolver>())
}


@RaptorDsl
public fun RaptorAssemblyQuery2<RaptorEntitiesComponent>.resolver(id: KClass<out RaptorEntityId>, resolverType: KType) {
	this { this.resolverTypes[id] = resolverType } // FIXME check collision
}
