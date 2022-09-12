package io.fluidsonic.raptor


@RaptorDsl
public fun interface RaptorAssemblyQuery2<out Value : Any> {

	@RaptorDsl
	public operator fun invoke(configure: Value.() -> Unit)
}


// TODO Make API indirect (authoring). `raptor.assembly.transform()`?
@RaptorDsl
public fun <In : Any, Out : Any> RaptorAssemblyQuery2<In>.map(transform: (In) -> Out): RaptorAssemblyQuery2<Out> =
	RaptorAssemblyQuery2 { configure ->
		this {
			transform(this).configure()
		}
	}


@RaptorDsl
public fun <In : Any, Out : Any> RaptorAssemblyQuery2<In>.flatMap(transform: (In) -> RaptorAssemblyQuery2<Out>): RaptorAssemblyQuery2<Out> =
	RaptorAssemblyQuery2 { configure ->
		this {
			transform(this)(configure)
		}
	}
