package io.fluidsonic.raptor


@Raptor.Dsl3
interface RaptorComponent<Self : RaptorComponent<Self>> : RaptorComponentSet<Self>
