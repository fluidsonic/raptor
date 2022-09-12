package io.fluidsonic.raptor


@RaptorDsl
public interface RaptorComponentSet2<out Component : RaptorComponent2> {

	@RaptorDsl
	public val all: RaptorAssemblyQuery2<Component>
}


@RaptorDsl
public val <Component : RaptorComponent2> RaptorAssemblyQuery2<RaptorComponentSet2<Component>>.all: RaptorAssemblyQuery2<Component>
	get() = flatMap { it.all }
