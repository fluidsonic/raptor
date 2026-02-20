package io.fluidsonic.raptor.service2

import io.fluidsonic.raptor.*
import io.fluidsonic.raptor.lifecycle.*
import kotlin.reflect.*


internal object Keys2 {

	val middlewareComponent = RaptorComponentKey<RaptorMiddlewareComponent>("middleware2")
	val servicesComponent = RaptorComponentKey<RaptorServiceComponent2<RaptorService2>>("services2")
}


@RaptorDsl
public inline fun <reified Service : RaptorService2> RaptorPluginScope<in RaptorLifecyclePlugin>.service2(): RaptorServiceComponent2<Service> =
	service2(Service::class.qualifiedName.orEmpty())


@RaptorDsl
public inline fun <reified Service : RaptorService2> RaptorPluginScope<in RaptorLifecyclePlugin>.service2(
	crossinline configure: context (RaptorServiceComponent2<Service>) () -> Unit,
): RaptorServiceComponent2<Service> =
	service2(Service::class.qualifiedName.orEmpty(), configure)


@RaptorDsl
public fun <Service : RaptorService2> RaptorPluginScope<in RaptorLifecyclePlugin>.service2(
	name: String,
): RaptorServiceComponent2<Service> {
	require(name.isNotEmpty()) { "Service name must not be empty." }

	val component = RaptorServiceComponent2<Service>(name = name)

	@Suppress("UNCHECKED_CAST")
	lifecycle.componentRegistry.register(Keys2.servicesComponent, component as RaptorServiceComponent2<RaptorService2>)

	return component
}


@RaptorDsl
public inline fun <Service : RaptorService2> RaptorPluginScope<in RaptorLifecyclePlugin>.service2(
	name: String,
	crossinline configure: context (RaptorServiceComponent2<Service>) () -> Unit,
): RaptorServiceComponent2<Service> =
	service2<Service>(name).apply(configure)


@JvmName("service0")
@RaptorDsl
public inline fun <reified Service : RaptorService2>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service2(
	constructor: KFunction0<Service>,
	crossinline configure: context (RaptorServiceComponent2<Service>) () -> Unit = {},
): RaptorServiceComponent2<Service> =
	service2 {
		factory { constructor() }

		configure()
	}


@JvmName("service1")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService2,
		reified A1,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service2(
	constructor: KFunction1<A1, Service>,
	crossinline configure: context (RaptorServiceComponent2<Service>) () -> Unit = {},
): RaptorServiceComponent2<Service> =
	service2 {
		factory { constructor(di()) }

		configure()
	}


@JvmName("service2")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService2,
		reified A1, reified A2,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service2(
	constructor: KFunction2<A1, A2, Service>,
	crossinline configure: context (RaptorServiceComponent2<Service>) () -> Unit = {},
): RaptorServiceComponent2<Service> =
	service2 {
		factory { constructor(di(), di()) }

		configure()
	}


@JvmName("service3")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService2,
		reified A1, reified A2, reified A3,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service2(
	constructor: KFunction3<A1, A2, A3, Service>,
	crossinline configure: context (RaptorServiceComponent2<Service>) () -> Unit = {},
): RaptorServiceComponent2<Service> =
	service2 {
		factory { constructor(di(), di(), di()) }

		configure()
	}


@JvmName("service4")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService2,
		reified A1, reified A2, reified A3, reified A4,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service2(
	constructor: KFunction4<A1, A2, A3, A4, Service>,
	crossinline configure: context (RaptorServiceComponent2<Service>) () -> Unit = {},
): RaptorServiceComponent2<Service> =
	service2 {
		factory { constructor(di(), di(), di(), di()) }

		configure()
	}


@JvmName("service5")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService2,
		reified A1, reified A2, reified A3, reified A4, reified A5,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service2(
	constructor: KFunction5<A1, A2, A3, A4, A5, Service>,
	crossinline configure: context (RaptorServiceComponent2<Service>) () -> Unit = {},
): RaptorServiceComponent2<Service> =
	service2 {
		factory { constructor(di(), di(), di(), di(), di()) }

		configure()
	}


@JvmName("service6")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService2,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service2(
	constructor: KFunction6<A1, A2, A3, A4, A5, A6, Service>,
	crossinline configure: context (RaptorServiceComponent2<Service>) () -> Unit = {},
): RaptorServiceComponent2<Service> =
	service2 {
		factory { constructor(di(), di(), di(), di(), di(), di()) }

		configure()
	}


@JvmName("service7")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService2,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service2(
	constructor: KFunction7<A1, A2, A3, A4, A5, A6, A7, Service>,
	crossinline configure: context (RaptorServiceComponent2<Service>) () -> Unit = {},
): RaptorServiceComponent2<Service> =
	service2 {
		factory { constructor(di(), di(), di(), di(), di(), di(), di()) }

		configure()
	}


@JvmName("service8")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService2,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service2(
	constructor: KFunction8<A1, A2, A3, A4, A5, A6, A7, A8, Service>,
	crossinline configure: context (RaptorServiceComponent2<Service>) () -> Unit = {},
): RaptorServiceComponent2<Service> =
	service2 {
		factory { constructor(di(), di(), di(), di(), di(), di(), di(), di()) }

		configure()
	}


@JvmName("service9")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService2,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service2(
	constructor: KFunction9<A1, A2, A3, A4, A5, A6, A7, A8, A9, Service>,
	crossinline configure: context (RaptorServiceComponent2<Service>) () -> Unit = {},
): RaptorServiceComponent2<Service> =
	service2 {
		factory { constructor(di(), di(), di(), di(), di(), di(), di(), di(), di()) }

		configure()
	}


@JvmName("service10")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService2,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service2(
	constructor: KFunction10<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, Service>,
	crossinline configure: context (RaptorServiceComponent2<Service>) () -> Unit = {},
): RaptorServiceComponent2<Service> =
	service2 {
		factory { constructor(di(), di(), di(), di(), di(), di(), di(), di(), di(), di()) }

		configure()
	}


@JvmName("service11")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService2,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10,
		reified A11,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service2(
	constructor: KFunction11<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, Service>,
	crossinline configure: context (RaptorServiceComponent2<Service>) () -> Unit = {},
): RaptorServiceComponent2<Service> =
	service2 {
		factory {
			constructor(
				di(), di(), di(), di(), di(), di(), di(), di(), di(), di(),
				di(),
			)
		}

		configure()
	}


@JvmName("service12")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService2,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10,
		reified A11, reified A12,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service2(
	constructor: KFunction12<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, Service>,
	crossinline configure: context (RaptorServiceComponent2<Service>) () -> Unit = {},
): RaptorServiceComponent2<Service> =
	service2 {
		factory {
			constructor(
				di(), di(), di(), di(), di(), di(), di(), di(), di(), di(),
				di(), di(),
			)
		}

		configure()
	}


@JvmName("service13")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService2,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10,
		reified A11, reified A12, reified A13,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service2(
	constructor: KFunction13<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, Service>,
	crossinline configure: context (RaptorServiceComponent2<Service>) () -> Unit = {},
): RaptorServiceComponent2<Service> =
	service2 {
		factory {
			constructor(
				di(), di(), di(), di(), di(), di(), di(), di(), di(), di(),
				di(), di(), di(),
			)
		}

		configure()
	}


@JvmName("service14")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService2,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10,
		reified A11, reified A12, reified A13, reified A14,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service2(
	constructor: KFunction14<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, Service>,
	crossinline configure: context (RaptorServiceComponent2<Service>) () -> Unit = {},
): RaptorServiceComponent2<Service> =
	service2 {
		factory {
			constructor(
				di(), di(), di(), di(), di(), di(), di(), di(), di(), di(),
				di(), di(), di(), di(),
			)
		}

		configure()
	}


@JvmName("service15")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService2,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10,
		reified A11, reified A12, reified A13, reified A14, reified A15,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service2(
	constructor: KFunction15<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, Service>,
	crossinline configure: context (RaptorServiceComponent2<Service>) () -> Unit = {},
): RaptorServiceComponent2<Service> =
	service2 {
		factory {
			constructor(
				di(), di(), di(), di(), di(), di(), di(), di(), di(), di(),
				di(), di(), di(), di(), di(),
			)
		}

		configure()
	}


@JvmName("service16")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService2,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10,
		reified A11, reified A12, reified A13, reified A14, reified A15, reified A16,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service2(
	constructor: KFunction16<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, Service>,
	crossinline configure: context (RaptorServiceComponent2<Service>) () -> Unit = {},
): RaptorServiceComponent2<Service> =
	service2 {
		factory {
			constructor(
				di(), di(), di(), di(), di(), di(), di(), di(), di(), di(),
				di(), di(), di(), di(), di(), di(),
			)
		}

		configure()
	}


@JvmName("service17")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService2,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10,
		reified A11, reified A12, reified A13, reified A14, reified A15, reified A16, reified A17,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service2(
	constructor: KFunction17<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, Service>,
	crossinline configure: context (RaptorServiceComponent2<Service>) () -> Unit = {},
): RaptorServiceComponent2<Service> =
	service2 {
		factory {
			constructor(
				di(), di(), di(), di(), di(), di(), di(), di(), di(), di(),
				di(), di(), di(), di(), di(), di(), di(),
			)
		}

		configure()
	}


@JvmName("service18")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService2,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10,
		reified A11, reified A12, reified A13, reified A14, reified A15, reified A16, reified A17, reified A18,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service2(
	constructor: KFunction18<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, Service>,
	crossinline configure: context (RaptorServiceComponent2<Service>) () -> Unit = {},
): RaptorServiceComponent2<Service> =
	service2 {
		factory {
			constructor(
				di(), di(), di(), di(), di(), di(), di(), di(), di(), di(),
				di(), di(), di(), di(), di(), di(), di(), di(),
			)
		}

		configure()
	}


@JvmName("service19")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService2,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10,
		reified A11, reified A12, reified A13, reified A14, reified A15, reified A16, reified A17, reified A18, reified A19,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service2(
	constructor: KFunction19<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, Service>,
	crossinline configure: context (RaptorServiceComponent2<Service>) () -> Unit = {},
): RaptorServiceComponent2<Service> =
	service2 {
		factory {
			constructor(
				di(), di(), di(), di(), di(), di(), di(), di(), di(), di(),
				di(), di(), di(), di(), di(), di(), di(), di(), di(),
			)
		}

		configure()
	}


@JvmName("service20")
@RaptorDsl
public inline fun
	<
		reified Service : RaptorService2,
		reified A1, reified A2, reified A3, reified A4, reified A5, reified A6, reified A7, reified A8, reified A9, reified A10,
		reified A11, reified A12, reified A13, reified A14, reified A15, reified A16, reified A17, reified A18, reified A19, reified A20,
		>
	RaptorPluginScope<in RaptorLifecyclePlugin>.service2(
	constructor: KFunction20<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20, Service>,
	crossinline configure: context (RaptorServiceComponent2<Service>) () -> Unit = {},
): RaptorServiceComponent2<Service> =
	service2 {
		factory {
			constructor(
				di(), di(), di(), di(), di(), di(), di(), di(), di(), di(),
				di(), di(), di(), di(), di(), di(), di(), di(), di(), di(),
			)
		}

		configure()
	}
