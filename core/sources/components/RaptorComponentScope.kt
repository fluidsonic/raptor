package io.fluidsonic.raptor


@Raptor.Dsl3
interface RaptorComponentScope<out Component : RaptorComponent> {

	@Raptor.Dsl3
	val raptorComponentSelection: Selection<Component>


	companion object {

		@Suppress("UNCHECKED_CAST")
		internal fun <Component : RaptorComponent> empty() =
			Empty as Collection<Component>


		internal fun <Component : RaptorComponent> asCollection(scope: RaptorComponentScope<Component>) =
			scope as? Collection<Component> ?: AsCollection(scope)
	}


	interface Collection<out Component : RaptorComponent> : RaptorComponentScope<Component> {

		@Raptor.Dsl3
		override val raptorComponentSelection: Selection.Collection<Component>
	}


	private class AsCollection<out Component : RaptorComponent>(val scope: RaptorComponentScope<Component>) :
		Collection<Component>,
		Selection.Collection<Component> {

		override fun filter(predicate: RaptorComponentRegistration<Component>.() -> Boolean): Collection<Component> =
			scope.raptorComponentSelection.filter(predicate)


		override fun invoke(configure: RaptorComponentRegistration.Mutable<Component>.() -> Unit) {
			raptorComponentSelection(configure)
		}


		override fun <Transformed : RaptorComponent> map(
			transform: RaptorComponentRegistration.Mutable<Component>.() -> RaptorComponentScope<Transformed>
		): Collection<Transformed> =
			scope.raptorComponentSelection.map(transform)


		override val raptorComponentSelection
			get() = this
	}


	private object Empty : Collection<RaptorComponent> {

		override val raptorComponentSelection = Selection.empty<RaptorComponent>()
	}


	interface Selection<out Component : RaptorComponent> {

		fun filter(predicate: RaptorComponentRegistration<Component>.() -> Boolean): RaptorComponentScope.Collection<Component>

		operator fun invoke(configure: RaptorComponentRegistration.Mutable<Component>.() -> Unit)

		fun <Transformed : RaptorComponent> map(
			transform: RaptorComponentRegistration.Mutable<Component>.() -> RaptorComponentScope<Transformed>
		): RaptorComponentScope.Collection<Transformed>


		companion object {

			@Suppress("UNCHECKED_CAST")
			internal fun <Component : RaptorComponent> empty() =
				Empty as Collection<Component>


			private object Empty : Collection<RaptorComponent> {

				override fun filter(predicate: RaptorComponentRegistration<RaptorComponent>.() -> Boolean) =
					RaptorComponentScope.empty<RaptorComponent>()


				override fun invoke(configure: RaptorComponentRegistration.Mutable<RaptorComponent>.() -> Unit) =
					Unit


				@Suppress("UNCHECKED_CAST")
				override fun <Transformed : RaptorComponent> map(
					transform: RaptorComponentRegistration.Mutable<RaptorComponent>.() -> RaptorComponentScope<Transformed>
				) =
					RaptorComponentScope.empty<Transformed>()
			}
		}


		interface Collection<out Component : RaptorComponent> : Selection<Component>
	}
}


@Raptor.Dsl3
operator fun <Scope : RaptorComponentScope<*>> Scope.invoke(configure: Scope.() -> Unit) {
	configure()
}
