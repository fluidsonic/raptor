package io.fluidsonic.raptor.graph


public sealed class RaptorUnion2<out _T1 : Any, out _T2 : Any> {

	public abstract val value: Any // _T1 | _T2


	public data class T1<_T1 : Any>(override val value: _T1) : RaptorUnion2<_T1, Nothing>()
	public data class T2<_T2 : Any>(override val value: _T2) : RaptorUnion2<Nothing, _T2>()
}
