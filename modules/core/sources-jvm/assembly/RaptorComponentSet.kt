package io.fluidsonic.raptor


@RaptorDsl
public interface RaptorComponentSet<out Component : RaptorComponent<out Component>> {

	@RaptorDsl
	public val all: RaptorAssemblyQuery<Component>
}


@RaptorDsl
public val <Component : RaptorComponent<Component>> RaptorAssemblyQuery<RaptorComponentSet<Component>>.all: RaptorAssemblyQuery<Component>
	get() = flatMap { it.all }
