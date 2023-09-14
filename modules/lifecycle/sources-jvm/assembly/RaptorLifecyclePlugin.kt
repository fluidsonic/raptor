package io.fluidsonic.raptor.lifecycle

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.di.*
import kotlin.coroutines.*
import kotlin.reflect.*


public object RaptorLifecyclePlugin : RaptorPluginWithConfiguration<RaptorLifecyclePluginConfiguration> {

	override fun RaptorPluginCompletionScope.complete(): RaptorLifecyclePluginConfiguration {
		val component = componentRegistry.one(Keys.lifecycleComponent)
		val serviceRegistrations = component.serviceRegistrations()

		if (serviceRegistrations.isNotEmpty())
			configure(RaptorDIPlugin) {
				di {
					for (registration in serviceRegistrations)
						registration.install(this)
				}
			}

		return RaptorLifecyclePluginConfiguration(
			serviceControllers = serviceRegistrations.map { registration ->
				RaptorServiceController(
					diKey = registration.diKey,
					name = registration.name,
				)
			}
		)
	}


	override fun RaptorPluginInstallationScope.install() {
		componentRegistry.register(Keys.lifecycleComponent, RaptorLifecycleComponent())

		optional(RaptorDIPlugin) {
			di {
				provide<CoroutineContext> { get<RaptorLifecycle>().coroutineContext }
				provide<RaptorLifecycle> { context.lifecycle }
			}
		}

		lifecycle {
			onStart("service creations") { context.lifecycle.createServices() }
			onStart("services", Int.MIN_VALUE + 1) { context.lifecycle.startServices() }
			onStop("services", Int.MAX_VALUE) { context.lifecycle.stopServices() }
		}
	}


	override fun toString(): String = "lifecycle"
}


@RaptorDsl
public val RaptorPluginScope<in RaptorLifecyclePlugin>.lifecycle: RaptorLifecycleComponent
	get() = componentRegistry.oneOrNull(Keys.lifecycleComponent) ?: throw RaptorPluginNotInstalledException(RaptorLifecyclePlugin)


@RaptorDsl
public fun <Service : RaptorService> RaptorPluginScope<in RaptorLifecyclePlugin>.service(
	name: String,
	factory: RaptorDI.() -> Service,
): RaptorServiceComponent<Service> =
	lifecycle.service(name = name, factory = factory)


@RaptorDsl
public inline fun <reified Service : RaptorService> RaptorPluginScope<in RaptorLifecyclePlugin>.service(
	noinline factory: RaptorDI.() -> Service,
): RaptorServiceComponent<Service> =
	service(name = Service::class.qualifiedName ?: "<anonymous service>", factory = factory)


@JvmName("service0")
@RaptorDsl
public inline fun
	<reified Service : RaptorService>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service(
	factory: KFunction0<Service>,
): RaptorServiceComponent<Service> =
	service { factory() }


@JvmName("service1")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService,
		reified A1,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service(
	factory: KFunction1<A1, Service>,
): RaptorServiceComponent<Service> =
	service { factory(get()) }


@JvmName("service2")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService,
		reified A1, reified A2,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service(
	factory: KFunction2<A1, A2, Service>,
): RaptorServiceComponent<Service> =
	service { factory(get(), get()) }


@JvmName("service3")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService,
		reified A1, reified A2, reified A3,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service(
	factory: KFunction3<A1, A2, A3, Service>,
): RaptorServiceComponent<Service> =
	service { factory(get(), get(), get()) }


@JvmName("service4")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService,
		reified A1, reified A2, reified A3, reified A4,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service(
	factory: KFunction4<A1, A2, A3, A4, Service>,
): RaptorServiceComponent<Service> =
	service { factory(get(), get(), get(), get()) }


@JvmName("service5")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService,
		reified A1, reified A2, reified A3, reified A4, reified A5,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service(
	factory: KFunction5<A1, A2, A3, A4, A5, Service>,
): RaptorServiceComponent<Service> =
	service { factory(get(), get(), get(), get(), get()) }


@JvmName("service6")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service(
	factory: KFunction6<A1, A2, A3, A4, A5, A6, Service>,
): RaptorServiceComponent<Service> =
	service { factory(get(), get(), get(), get(), get(), get()) }


@JvmName("service7")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service(
	factory: KFunction7<A1, A2, A3, A4, A5, A6, A7, Service>,
): RaptorServiceComponent<Service> =
	service { factory(get(), get(), get(), get(), get(), get(), get()) }


@JvmName("service8")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service(
	factory: KFunction8<A1, A2, A3, A4, A5, A6, A7, A8, Service>,
): RaptorServiceComponent<Service> =
	service { factory(get(), get(), get(), get(), get(), get(), get(), get()) }


@JvmName("service9")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service(
	factory: KFunction9<A1, A2, A3, A4, A5, A6, A7, A8, A9, Service>,
): RaptorServiceComponent<Service> =
	service { factory(get(), get(), get(), get(), get(), get(), get(), get(), get()) }


@JvmName("service10")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service(
	factory: KFunction10<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, Service>,
): RaptorServiceComponent<Service> =
	service { factory(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }


@JvmName("service11")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10,
		reified A11,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service(
	factory: KFunction11<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, Service>,
): RaptorServiceComponent<Service> =
	service {
		factory(
			get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
			get(),
		)
	}


@JvmName("service12")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10,
		reified A11, reified A12,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service(
	factory: KFunction12<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, Service>,
): RaptorServiceComponent<Service> =
	service {
		factory(
			get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
			get(), get(),
		)
	}


@JvmName("service13")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10,
		reified A11, reified A12, reified A13,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service(
	factory: KFunction13<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, Service>,
): RaptorServiceComponent<Service> =
	service {
		factory(
			get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
			get(), get(), get(),
		)
	}


@JvmName("service14")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10,
		reified A11, reified A12, reified A13, reified A14,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service(
	factory: KFunction14<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, Service>,
): RaptorServiceComponent<Service> =
	service {
		factory(
			get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
			get(), get(), get(), get(),
		)
	}


@JvmName("service15")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10,
		reified A11, reified A12, reified A13, reified A14, reified A15,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service(
	factory: KFunction15<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Service>,
): RaptorServiceComponent<Service> =
	service {
		factory(
			get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
			get(), get(), get(), get(), get(),
		)
	}


@JvmName("service16")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10,
		reified A11, reified A12, reified A13, reified A14, reified A15, reified A16,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service(
	factory: KFunction16<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, Service>,
): RaptorServiceComponent<Service> =
	service {
		factory(
			get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
			get(), get(), get(), get(), get(), get(),
		)
	}


@JvmName("service17")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10,
		reified A11, reified A12, reified A13, reified A14, reified A15, reified A16, reified A17,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service(
	factory: KFunction17<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, Service>,
): RaptorServiceComponent<Service> =
	service {
		factory(
			get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
			get(), get(), get(), get(), get(), get(), get(),
		)
	}


@JvmName("service18")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10,
		reified A11, reified A12, reified A13, reified A14, reified A15, reified A16, reified A17, reified A18,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service(
	factory: KFunction18<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, Service>,
): RaptorServiceComponent<Service> =
	service {
		factory(
			get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
			get(), get(), get(), get(), get(), get(), get(), get(),
		)
	}


@JvmName("service19")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10,
		reified A11, reified A12, reified A13, reified A14, reified A15, reified A16, reified A17, reified A18, reified A19,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service(
	factory: KFunction19<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, Service>,
): RaptorServiceComponent<Service> =
	service {
		factory(
			get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
			get(), get(), get(), get(), get(), get(), get(), get(), get(),
		)
	}


@JvmName("service20")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10,
		reified A11, reified A12, reified A13, reified A14, reified A15, reified A16, reified A17, reified A18, reified A19, reified A20,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service(
	factory: KFunction20<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, Service>,
): RaptorServiceComponent<Service> =
	service {
		factory(
			get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
			get(), get(), get(), get(), get(), get(), get(), get(), get(), get(),
		)
	}
