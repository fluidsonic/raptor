package io.fluidsonic.raptor


@RaptorDsl
public fun interface RaptorAssemblyQuery2<out Element : Any> {

	@RaptorDsl
	public fun each(configure: Element.() -> Unit)
}


@RaptorDsl
public operator fun <Element : Any> RaptorAssemblyQuery2<Element>.invoke(configure: Element.() -> Unit) {
	each(configure)
}


@RaptorDsl
public fun <In : Any, Out : Any> RaptorAssemblyQuery2<In>.flatMap(
	transform: (In) -> RaptorAssemblyQuery2<Out>,
): RaptorAssemblyQuery2<Out> =
	RaptorAssemblyQuery2 { configure ->
		each {
			transform(this).each(configure)
		}
	}


// TODO Make API indirect (authoring). `raptor.assembly.transform()`?
@RaptorDsl
public fun <In : Any, Out : Any> RaptorAssemblyQuery2<In>.map(
	transform: (In) -> Out,
): RaptorAssemblyQuery2<Out> =
	RaptorAssemblyQuery2 { configure ->
		each {
			transform(this).configure()
		}
	}
