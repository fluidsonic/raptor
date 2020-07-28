package io.fluidsonic.raptor

import freemarker.core.*
import freemarker.template.*
import java.util.concurrent.*
import kotlin.reflect.*


internal class DelegatingFreemarkerObjectWrapper(
	wrappersByClass: Map<KClass<*>, RaptorFreemarkerObjectWrapper<*>>
) : DefaultObjectWrapper(Configuration.VERSION_2_3_30) {

	private val wrappersByClass: MutableMap<Class<*>, RaptorFreemarkerObjectWrapper<*>> = wrappersByClass.mapKeysTo(ConcurrentHashMap()) { it.key.java }


	override fun handleUnknownType(obj: Any): TemplateModel {
		val wrapper = wrapperForClass(obj::class.java) ?: run {
			return super.handleUnknownType(obj)
		}

		// FIXME take over the side that sets this
		val context = Environment.getCurrentEnvironment().getCustomState(RaptorContext::class) as RaptorContext

		return wrapper.wrap(value = obj, baseWrapper = this, context = context)
	}


	@Suppress("UNCHECKED_CAST")
	private fun wrapperForClass(valueClass: Class<*>): RaptorFreemarkerObjectWrapper<Any>? {
		wrappersByClass[valueClass]?.let { wrapper ->
			return wrapper as RaptorFreemarkerObjectWrapper<Any>
		}

		return wrappersByClass.entries.firstOrNull { it.key.isAssignableFrom(valueClass) }?.value
			?.let { it as RaptorFreemarkerObjectWrapper<Any> }
			?.also { wrappersByClass[valueClass] = it }
	}
}
