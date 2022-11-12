package io.fluidsonic.raptor.lifecycle

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import kotlin.reflect.*


public class RaptorServiceComponent<Service : RaptorService> internal constructor(
	private val factory: RaptorDI.() -> Service,
	private val name: String,
) : RaptorComponent.Base<RaptorServiceComponent<Service>>(RaptorLifecyclePlugin) {

	private val diKey = ServiceDIKey<Service>(name)
	private val providedKeys: MutableList<RaptorDIKey<in Service>> = mutableListOf()


	@RaptorDsl
	public fun provides(key: RaptorDIKey<in Service>): RaptorServiceComponent<Service> =
		apply {
			providedKeys += key
		}


	// https://youtrack.jetbrains.com/issue/KT-209/Add-lower-bounds-for-generic-type-parameters-at-first-consider-it
	@RaptorDsl
	public inline fun <reified Type : KClass<in Service>> provides(@Suppress("UNUSED_PARAMETER") type: Type): RaptorServiceComponent<Service> =
		provides(RaptorDIKey<Service>(checkNotNull(typeOf<Type>().arguments.single().type)))


	internal fun registration(): RaptorServiceRegistration<Service> =
		RaptorServiceRegistration(
			factory = factory,
			name = name,
			providedKeys = providedKeys.toList(),
		)


	override fun toString(): String = "service '$name'"
}
