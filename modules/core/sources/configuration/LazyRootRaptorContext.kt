package io.fluidsonic.raptor


internal class LazyRootRaptorContext : RaptorContext {

	private var delegate: RaptorContext? = null


	override val context: RaptorContext
		get() = requireDelegate()


	override val parent: RaptorContext?
		get() = null


	override val properties: RaptorPropertySet
		get() = requireDelegate().properties


	private fun requireDelegate() =
		delegate ?: error("This context cannot be used until the configuration of all components and features has completed.")


	fun resolve(context: RaptorContext) {
		check(delegate == null)

		delegate = context
	}


	override fun toString() =
		delegate?.toString() ?: "<lazy context waiting for configuration to complete>"
}
