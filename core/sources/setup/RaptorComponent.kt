package io.fluidsonic.raptor


@Raptor.Dsl3
interface RaptorComponent {

	interface Taggable : RaptorComponent {

		val raptorTags: Set<Any>
	}
}
