package io.fluidsonic.raptor


internal class RaptorComponentRegistrationImpl<out Component : RaptorComponent>(
	override val component: Component,
	override val registry: RaptorComponentRegistryImpl
) : RaptorComponentRegistration.Mutable<Component>, RaptorComponentScope.Collection<Component>, RaptorComponentScope.Selection.Collection<Component> {

	override val raptorComponentSelection: RaptorComponentScope.Selection.Collection<Component>
		get() = this


	override fun filter(predicate: RaptorComponentRegistration<Component>.() -> Boolean): RaptorComponentScope.Collection<Component> =
		if (predicate(this)) this
		else RaptorComponentScope.empty()


	override fun invoke(configure: RaptorComponentRegistration.Mutable<Component>.() -> Unit) {
		apply(configure)
	}


	override fun <Transformed : RaptorComponent> map(
		transform: RaptorComponentRegistration.Mutable<Component>.() -> RaptorComponentScope<Transformed>
	): RaptorComponentScope.Collection<Transformed> =
		RaptorComponentScope.asCollection(transform(this))


	class Collection<Component : RaptorComponent>(
		private val registrations: MutableList<RaptorComponentRegistrationImpl<Component>> = mutableListOf()
	) :
		RaptorComponentScope.Collection<Component>,
		RaptorComponentScope.Selection.Collection<Component>,
		List<RaptorComponentRegistrationImpl<Component>> by registrations {

		private val configurations: MutableList<RaptorComponentRegistration.Mutable<Component>.() -> Unit> = mutableListOf()


		fun addComponent(
			component: Component,
			registry: RaptorComponentRegistryImpl
		): RaptorComponentRegistrationImpl<Component> {
			registrations.firstOrNull { it.component === component }?.let { existingRegistration ->
				error("Cannot register component of ${component::class} since it has already been registered: ${existingRegistration.component}")
			}

			val registration = RaptorComponentRegistrationImpl(
				component = component,
				registry = registry
			)
			registrations += registration

			val configurations = configurations

			with(registration) {
				// We iterate over indices because the configurations we call may append more configurations.
				for (index in configurations.indices)
					configurations[index]()
			}

			return registration
		}


		override fun filter(predicate: RaptorComponentRegistration<Component>.() -> Boolean): RaptorComponentScope.Collection<Component> =
			Filtered(predicate = predicate, source = this)


		override fun invoke(configure: RaptorComponentRegistration.Mutable<Component>.() -> Unit) {
			configurations += configure

			// We iterate over indices because the configuration we call may append more components.
			for (index in registrations.indices)
				registrations[index].apply(configure)
		}


		override fun <Transformed : RaptorComponent> map(
			transform: RaptorComponentRegistration.Mutable<Component>.() -> RaptorComponentScope<Transformed>
		): RaptorComponentScope.Collection<Transformed> =
			Mapped(transform = transform, source = this)


		override val raptorComponentSelection: RaptorComponentScope.Selection.Collection<Component>
			get() = this


		private class Filtered<Component : RaptorComponent>(
			private val predicate: RaptorComponentRegistration<Component>.() -> Boolean,
			private val source: RaptorComponentScope.Collection<Component>
		) : RaptorComponentScope.Collection<Component>, RaptorComponentScope.Selection.Collection<Component> {

			override fun filter(predicate: RaptorComponentRegistration<Component>.() -> Boolean): RaptorComponentScope.Collection<Component> =
				Filtered(predicate = predicate, source = this)


			override fun invoke(configure: RaptorComponentRegistration.Mutable<Component>.() -> Unit) {
				source.raptorComponentSelection {
					if (this@Filtered.predicate(this))
						configure()
				}
			}


			override fun <Transformed : RaptorComponent> map(
				transform: RaptorComponentRegistration.Mutable<Component>.() -> RaptorComponentScope<Transformed>
			): RaptorComponentScope.Collection<Transformed> =
				Mapped(transform = transform, source = this)


			override val raptorComponentSelection: RaptorComponentScope.Selection.Collection<Component>
				get() = this
		}


		private class Mapped<SourceComponent : RaptorComponent, Component : RaptorComponent>(
			private val transform: RaptorComponentRegistration.Mutable<SourceComponent>.() -> RaptorComponentScope<Component>,
			private val source: RaptorComponentScope.Collection<SourceComponent>
		) : RaptorComponentScope.Collection<Component>, RaptorComponentScope.Selection.Collection<Component> {

			override fun filter(predicate: RaptorComponentRegistration<Component>.() -> Boolean): RaptorComponentScope.Collection<Component> =
				Filtered(predicate = predicate, source = this)


			override fun invoke(configure: RaptorComponentRegistration.Mutable<Component>.() -> Unit) {
				source.raptorComponentSelection {
					this@Mapped.transform(this).raptorComponentSelection {
						configure()
					}
				}
			}


			override fun <Transformed : RaptorComponent> map(
				transform: RaptorComponentRegistration.Mutable<Component>.() -> RaptorComponentScope<Transformed>
			): RaptorComponentScope.Collection<Transformed> =
				Mapped(transform = transform, source = this)


			override val raptorComponentSelection: RaptorComponentScope.Selection.Collection<Component>
				get() = this
		}
	}
}
