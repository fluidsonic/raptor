package io.fluidsonic.raptor


@RaptorDsl
public fun interface RaptorAssemblyQuery<out Element : Any> {

	@RaptorDsl
	public fun each(configure: Element.() -> Unit)
}


@RaptorDsl
public operator fun <Element : Any> RaptorAssemblyQuery<Element>.invoke(configure: Element.() -> Unit) {
	each(configure)
}


@RaptorDsl
public fun <Element : Any> RaptorAssemblyQuery<Element>.filter(
	predicate: (Element) -> Boolean,
): RaptorAssemblyQuery<Element> =
	RaptorAssemblyQuery { configure ->
		each {
			if (predicate(this))
				configure()
		}
	}


@RaptorDsl
public fun <In : Any, Out : Any> RaptorAssemblyQuery<In>.flatMap(
	transform: (In) -> RaptorAssemblyQuery<Out>,
): RaptorAssemblyQuery<Out> =
	RaptorAssemblyQuery { configure ->
		each {
			transform(this).each(configure)
		}
	}


// TODO Make API indirect (authoring). `raptor.assembly.transform()`?
@RaptorDsl
public fun <In : Any, Out : Any> RaptorAssemblyQuery<In>.map(
	transform: (In) -> Out,
): RaptorAssemblyQuery<Out> =
	RaptorAssemblyQuery { configure ->
		each {
			transform(this).configure()
		}
	}
