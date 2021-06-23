package io.fluidsonic.raptor

import kotlin.collections.set
import kotlin.reflect.*


public class RaptorEntitiesComponent internal constructor() : RaptorComponent.Default<RaptorEntitiesComponent>() {

	internal val resolverTypes: MutableMap<KClass<out RaptorEntityId>, KType> = hashMapOf()


	internal object Key : RaptorComponentKey<RaptorEntitiesComponent> {

		override fun toString() = "entities"
	}
}


// FIXME
@OptIn(ExperimentalStdlibApi::class)
@RaptorDsl
public inline fun <reified Id : RaptorEntityId, reified Resolver : RaptorEntityResolver<*, Id>> RaptorComponentSet<RaptorEntitiesComponent>.resolver() {
	resolver(id = Id::class, resolverType = typeOf<Resolver>())
}


@RaptorDsl
public fun RaptorComponentSet<RaptorEntitiesComponent>.resolver(id: KClass<out RaptorEntityId>, resolverType: KType) {
	configure { this.resolverTypes[id] = resolverType } // FIXME check collision
}
